package parser;

import java.util.LinkedList;

import util.Flist;
import lexer.Lexer;
import lexer.Token;
import lexer.Token.Kind;

public class Parser
{
	Lexer 		lexer;
	Token 		current;
	Token       pretoken=null,nextoken=null;
	String 		fname;

	/*  elements for mainclass  */
	ast.mainClass.MainClass mainclass=null;
	String 		mainid=null;
	String 		mainarg=null;
	ast.stm.T 	mainstm=null;
	ast.exp.T   mainexp=null;
	
	/* elements for classes     */
	LinkedList<ast.classs.T> classes=null;
	String 	    classid=null;
	String		classextends=null;	
	java.util.LinkedList<ast.dec.T>      classdecs=null;
	java.util.LinkedList<ast.method.T>   classmethods=null;
	public ast.classs.Class tempclass=null;

	/* elements for methods		*/
	util.Flist<ast.method.T> tempmethod=null;
	
	
	
	
	
  public Parser(String fname, java.io.InputStream fstream)
  {
	this.fname=fname;
    lexer = new Lexer(fname, fstream);
    current = lexer.nextToken();
  }

  // /////////////////////////////////////////////
  // utility methods to connect the lexer
  // and the parser.

  private void advance()
  {
    current = lexer.nextToken();
  }

  private void eatToken(Kind kind)
  {
    if (kind == current.kind)
      advance();
    else {
      System.out.println("line is	"+current.lineNum); 
      System.out.println("Expects: " + kind.toString());
      System.out.println("But got: " + current.kind.toString());
      System.exit(1);
    }
  }
  
  private void eatToken(Kind kind,String line_info)
  {
    if (kind == current.kind)
      advance();
    else {
      System.out.println("line is	"+current.lineNum); 
      System.out.println("Expects: " + kind.toString());
      System.out.println("But got: " + current.kind.toString());
      System.out.println(line_info);
      System.exit(1);
    }
  }

  private void error()
  { 
    System.out.println("Syntax error: compilation aborting...\n");
    System.out.println("Error info:  \n\t"+fname+".java    at   line"+current.lineNum+"\n");
    System.exit(1);
    return;
  }

  // ////////////////////////////////////////////////////////////
  // below are method for parsing.

  // A bunch of parsing methods to parse expressions. The messy
  // parts are to deal with precedence and associativity.

  // ExpList -> Exp ExpRest*
  // ->
  // ExpRest -> , Exp
  

  private LinkedList parseExpList()
  {
	LinkedList<ast.exp.T> list=new LinkedList<ast.exp.T>(); 
	ast.exp.T exp=null; 
    if (current.kind == Kind.TOKEN_RPAREN)
      return null;
    exp=parseExp();
    
    list.add(exp);
    while (current.kind == Kind.TOKEN_COMMER) {
      advance();
      list.add(parseExp());
    }
    return list;
  }

  // AtomExp -> (exp)
  // -> INTEGER_LITERAL
  // -> true
  // -> false
  // -> this
  // -> id
  // -> new int [exp]
  // -> new id ()
  private ast.exp.T parseAtomExp()
  {
	ast.exp.T exp;
	int num;
	String lexmer=null;
//  System.out.println(current.kind);
    switch (current.kind) {
    case TOKEN_LPAREN:
      advance();
      exp=parseExp();
      eatToken(Kind.TOKEN_RPAREN);
      return exp;
    case TOKEN_NUM:
      num=Integer.parseInt(current.lexeme);
      advance();
      return new ast.exp.Num(num);
    case TOKEN_TRUE:
      advance();
      return new ast.exp.True();
    case TOKEN_FALSE://TMD ¿Ó»õ¡£¡£¡£
        advance();
      return new ast.exp.False();
    case TOKEN_THIS:
      advance();
      return new ast.exp.This();
    case TOKEN_ID:
      lexmer =current.lexeme;
      advance();
      return new ast.exp.Id(lexmer);
    case TOKEN_NEW: {
      advance();
      switch (current.kind) {
      case TOKEN_INT:
        advance();
        eatToken(Kind.TOKEN_LBRACK);
        exp=parseExp();
        eatToken(Kind.TOKEN_RBRACK);
        return new ast.exp.NewIntArray(exp);
      case TOKEN_ID:
    	lexmer=current.lexeme;
        advance();
        eatToken(Kind.TOKEN_LPAREN);
        eatToken(Kind.TOKEN_RPAREN);
        return new ast.exp.NewObject(lexmer);
      default:
        error();
        return null;
      }
    }
    default:
      error();
      return null;
    }
  }

  // NotExp -> AtomExp
  // -> AtomExp .id (expList)
  // -> AtomExp [exp]
  // -> AtomExp .length
  private ast.exp.T parseNotExp()
  {
	ast.exp.T Atomexp=parseAtomExp(),exp=null;
	String lexmer=null;
	LinkedList<ast.exp.T> args=new LinkedList<ast.exp.T>();
	
    while (current.kind == Kind.TOKEN_DOT || current.kind == Kind.TOKEN_LBRACK) {
      if (current.kind == Kind.TOKEN_DOT) {
        advance();
        if (current.kind == Kind.TOKEN_LENGTH) {
          advance();
          return new ast.exp.Length(Atomexp);
        }
//        System.out.println("here?");
        lexmer=current.lexeme;
        eatToken(Kind.TOKEN_ID);
        eatToken(Kind.TOKEN_LPAREN);
        args=parseExpList();
        eatToken(Kind.TOKEN_RPAREN);
        return new ast.exp.Call(Atomexp, lexmer, args);
      } else {
//    	  System.out.println("here?");
        advance();
        exp=parseExp();
        eatToken(Kind.TOKEN_RBRACK);
        return new ast.exp.ArraySelect(Atomexp, exp);
      }
    }
    
    return Atomexp;
  }

  // TimesExp -> ! TimesExp
  // -> NotExp
  private ast.exp.T parseTimesExp()
  {
	int flag=0;  
	ast.exp.T exp;  
    while (current.kind == Kind.TOKEN_NOT) {
      advance();
      flag=1;
    }
    exp=parseNotExp();
    
    if(1==flag)
    {
    	return new ast.exp.Not(exp);
    }
    else
    {
    	return exp;
    }
  }

  // AddSubExp -> TimesExp * TimesExp
  // -> TimesExp
  private ast.exp.T parseAddSubExp()
  {
	ast.exp.T left=null,right=null;  
	
    left=parseTimesExp();
    
    while (current.kind == Kind.TOKEN_TIMES) {
      advance();
      right=parseTimesExp();
    }
    if(right!=null)
    {
    	return new ast.exp.Times(left, right);
    }
    else 
    {
    	return left;
    }
  }

  // LtExp -> AddSubExp + AddSubExp
  // -> AddSubExp - AddSubExp
  // -> AddSubExp
  private ast.exp.T parseLtExp()
  {
	int kind=0;
	ast.exp.T left=null,right=null;
    left=parseAddSubExp();
    while (current.kind == Kind.TOKEN_ADD || current.kind == Kind.TOKEN_SUB) {
    	if(current.kind==Kind.TOKEN_ADD)
    	{
    		kind=1;
    	}
    	else if(current.kind==Kind.TOKEN_SUB)
    	{
    		kind=2;
    	}
    	advance();
    	right=parseAddSubExp();
    }
    
    if(1==kind)
    {
    	return new ast.exp.Add(left, right);
    }else if(2==kind)
    {
    	return new ast.exp.Sub(left, right);
    }else 
    {
    	return left;
    }
  }

  // AndExp -> LtExp < LtExp
  // -> LtExp
  private ast.exp.T parseAndExp()
  { 
	ast.exp.T left=null,right=null;  
    left=parseLtExp();
    while (current.kind == Kind.TOKEN_LT) {
      advance();
      right=parseLtExp();
    }
    if(right!=null)
    {
    	return new ast.exp.Lt(left, right);
    }else
    {
    	return left;
    }
  }

  // Exp -> AndExp && AndExp
  // -> AndExp
  private ast.exp.T parseExp()
  { 
	ast.exp.T left=null,right=null;  
    left=parseAndExp();
    while (current.kind == Kind.TOKEN_AND) {
      advance();
      right=parseAndExp();
    }
    if(right!=null)
    {
    	return new ast.exp.And(left, right);
    }else 
    {   	
    	return left;
    }
  }

  // Statement -> { Statement* }
  // -> if ( Exp ) Statement else Statement
  // -> while ( Exp ) Statement
  // -> System.out.println ( Exp ) ;
  // -> id = Exp ;
  // -> id [ Exp ]= Exp ;


  public enum Branch{IF,WHILE,SYSTEM,IDASSIGN,IDARRY};

  Branch flag=null;

  LinkedList<ast.stm.T> list=null;
  	
  LinkedList<ast.stm.T> stms=new LinkedList<ast.stm.T>();
  LinkedList<ast.stm.T> stml=null;
  LinkedList<ast.stm.T> stmr=null;
  private ast.stm.T parseStatement()
  {
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a statement.
    //new util.Todo();

	//parseVarDecls();

	  
	if(current.kind==Kind.TOKEN_LBRACE)
	{
		eatToken(Kind.TOKEN_LBRACE);
		//parseStatement();
//		eatToken(Kind.TOKEN_RBRACE);
	}
	
	if(current.kind==Kind.TOKEN_IF)
	{
		
		flag=Branch.IF;
		eatToken(Kind.TOKEN_IF);
		eatToken(Kind.TOKEN_LPAREN);
		ast.exp.T exp=parseExp();
		eatToken(Kind.TOKEN_RPAREN);
		
		ast.stm.T stm1=null,stm2=null;
		if(current.kind==Kind.TOKEN_LBRACE)
		{
			eatToken(Kind.TOKEN_LBRACE);
			stml=new LinkedList<ast.stm.T>();
			
			while(current.kind!=Kind.TOKEN_RBRACE)
			{
				stml.add(parseStatement());
			}
			
		}else
			stm1=parseStatement();
		
		while(current.kind==Kind.TOKEN_RBRACE)
			eatToken(Kind.TOKEN_RBRACE);
		
		eatToken(Kind.TOKEN_ELSE);
		
		if(current.kind==Kind.TOKEN_LBRACE)
		{
			eatToken(Kind.TOKEN_LBRACE);
			stmr=new LinkedList<ast.stm.T>();
			
			while(current.kind!=Kind.TOKEN_RBRACE)
			{
				stmr.add(parseStatement());
			}
			
		}else
			stm2=parseStatement();
		
		if(stm1!=null&stm2!=null)
			return new ast.stm.If(exp, stm1, stm2);
		else if(stm1!=null&stm2==null)
		{
			return new ast.stm.If(exp, stm1, new ast.stm.Block(stmr));
		}else if(stm1==null&stm2!=null)
		{
			return new ast.stm.If(exp, new ast.stm.Block(stml), stm2);
		}else if(stm1==null&stm2==null)
		{
			return new ast.stm.If(exp, new ast.stm.Block(stml), new ast.stm.Block(stmr));
		}
		
		
	}
	
	if(current.kind==Kind.TOKEN_WHILE)
	{
		eatToken(Kind.TOKEN_WHILE);
		eatToken(Kind.TOKEN_LPAREN);
		ast.exp.T exp =	parseExp();
		eatToken(Kind.TOKEN_RPAREN);

		if(current.kind==Kind.TOKEN_LBRACE)/* while block problems  */
			eatToken(Kind.TOKEN_LBRACE);

		stms=null;
		ast.stm.T stml=null;
		
		if(current.kind==Kind.TOKEN_LBRACE)
		{
			eatToken(Kind.TOKEN_LBRACE);
			
			stms=new LinkedList<ast.stm.T>();
			while(current.kind!=Kind.TOKEN_RBRACE)
			{
				stms.add(parseStatement());
			}
		}else
			stml=parseStatement();
		
		if(stml==null)
			return new ast.stm.While(exp, new ast.stm.Block(stms));
		else
			return new ast.stm.While(exp, stml);	
	}
	  // Statement -> { Statement* }
	  // -> if ( Exp ) Statement else Statement
	  // -> while ( Exp ) Statement
	  // -> System.out.println ( Exp ) ;
	  // -> id = Exp ;
	  // -> id [ Exp ]= Exp ;
	if(current.kind==Kind.TOKEN_SYSTEM)
	{
		eatToken(Kind.TOKEN_SYSTEM);
		eatToken(Kind.TOKEN_DOT);
		eatToken(Kind.TOKEN_OUT);
		eatToken(Kind.TOKEN_DOT);
		eatToken(Kind.TOKEN_PRINTLN);
		eatToken(Kind.TOKEN_LPAREN);
		
		flag=Branch.SYSTEM;
		
		ast.exp.T exp;
		mainexp=exp=parseExp();
		
		if(exp!=null){
			
			mainstm=new ast.stm.Print(exp);
			//list.add(new ast.stm.Print(exp));
		}
		else{ 
			System.out.println("exp is null");
		}
			//		if(exp!=null)
//			list.add(new ast.stm.Print(exp));
		
		eatToken(Kind.TOKEN_RPAREN);
		eatToken(Kind.TOKEN_SEMI);
		
		return new ast.stm.Print(exp);
	}
	
	String id=current.lexeme;
	if(current.kind==Kind.TOKEN_ID)
	{		
		//eatToken(Kind.TOKEN_ID);
		if(nextoken!=null)
		{
			current=nextoken;
			nextoken=null;
		}else
			eatToken(Kind.TOKEN_ID);
		
		if(current.kind==Kind.TOKEN_ASSIGN)
		{
			flag=Branch.IDASSIGN;
			eatToken(Kind.TOKEN_ASSIGN);
			ast.exp.T exp=null;
			exp=parseExp();
			eatToken(Kind.TOKEN_SEMI);
			//parseStatement();
			
			return new ast.stm.Assign(id, exp);
		}
		// Statement -> { Statement* }
		  // -> if ( Exp ) Statement else Statement
		  // -> while ( Exp ) Statement
		  // -> System.out.println ( Exp ) ;
		  // -> id = Exp ;
		  // -> id [ Exp ]= Exp ;
		else{
			ast.exp.T exp1,exp2;
			flag=Branch.IDARRY;
			eatToken(Kind.TOKEN_LBRACK);
			exp1=parseExp();
			eatToken(Kind.TOKEN_RBRACK);
			eatToken(Kind.TOKEN_ASSIGN);
			exp2=parseExp();
			eatToken(Kind.TOKEN_SEMI);
			
			//parseStatement();
			return new ast.stm.AssignArray(id, exp1, exp2);
		}
	}
//	 System.out.println("error here line 295");
	
	return null;
	
  }

  // Statements -> Statement Statements
  // ->
  LinkedList<ast.stm.T> liststms=new LinkedList<ast.stm.T>();
  private LinkedList<ast.stm.T> parseStatements()
  {
	  
	liststms=new LinkedList<ast.stm.T>();
  	ast.stm.T st;
//	System.out.println("before    ?"+list.getFirst());
    while (current.kind == Kind.TOKEN_LBRACE || current.kind == Kind.TOKEN_IF
        || current.kind == Kind.TOKEN_WHILE
        || current.kind == Kind.TOKEN_SYSTEM || current.kind == Kind.TOKEN_ID) {
    	
    	
    	liststms.add(st=parseStatement());
    	
//    	System.out.println(st);
    	while(Kind.TOKEN_RBRACE==current.kind)
    	{
    		eatToken(Kind.TOKEN_RBRACE);
    	}

    }
    
    return liststms;
  }

  // Type -> int []
  // -> boolean
  // -> int
  // -> id
  private ast.type.T parseType()
  {
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a type.
    //new util.Todo();
	
	if(current.kind==Kind.TOKEN_INT)
	{
		eatToken(Kind.TOKEN_INT);
		if(current.kind==Kind.TOKEN_ARR)
		{
			eatToken(Kind.TOKEN_ARR);
			return new ast.type.IntArray();
		}
		
		return new ast.type.Int();
		
	}else if(current.kind==Kind.TOKEN_BOOLEAN)
	{
		eatToken(Kind.TOKEN_BOOLEAN);
		return new ast.type.Boolean();
		
	}else if(current.kind==Kind.TOKEN_ID)
	{
		String lexmer=current.lexeme;
		//eatToken(Kind.TOKEN_ID); 
		if(nextoken==null)
		{
			eatToken(Kind.TOKEN_ID); 
		}else
		{
			current=nextoken;
			nextoken=null;
		}
		
		if(current.kind==Kind.TOKEN_LBRACK)
		{
			eatToken(Kind.TOKEN_LBRACK);
			eatToken(Kind.TOKEN_NUM);
			eatToken(Kind.TOKEN_RBRACK);
		}
		return  new ast.type.Class(lexmer);
		
	}
	
	error();
	 return null;
  }

	ast.type.T type=null;
	ast.exp.T exp;
  // VarDecl -> Type id ;
  private ast.dec.T parseVarDecl()
  {
	String lexmer=null;

    // to parse the "Type" nonterminal in this method, instead of writing
    // a fresh one.
    type=parseType();
    if(current.kind==Kind.TOKEN_ASSIGN)
    {
    	eatToken(Kind.TOKEN_ASSIGN);
    	exp=parseExp();
//    	System.out.println("__here?__");
    	eatToken(Kind.TOKEN_SEMI,"466 assign value dec");
    	return null;
    }
    lexmer=current.lexeme;
    eatToken(Kind.TOKEN_ID,"here error?  412");
    
    eatToken(Kind.TOKEN_SEMI);
    return new ast.dec.Dec(type, lexmer);
  }

  // VarDecls -> VarDecl VarDecls
  // ->
  private LinkedList<ast.dec.T> parseVarDecls()
  {
	LinkedList<ast.dec.T> list=new LinkedList<ast.dec.T>();
    while (current.kind == Kind.TOKEN_INT || current.kind == Kind.TOKEN_BOOLEAN
    	 ||current.kind == Kind.TOKEN_ID) 
    {
    	//aux01   =    this.Init(sz);
    	//Tree    root ;
    	if(current.kind==Kind.TOKEN_ID)
    	{
    		pretoken=current;   		
    		eatToken(Kind.TOKEN_ID);
    		
			nextoken=current;
			
    		if(current.kind==Kind.TOKEN_ASSIGN)
    		{
    			current=pretoken;
    			return list;
    		}
    		else
    		{
    			current=pretoken;
    			list.add(parseVarDecl());
    		}
    	}
    	else   	
    		list.add(parseVarDecl());
//    	parseVarDecl();
    }
    return list;
  }

  // FormalList -> Type id FormalRest*
  // ->
  // FormalRest -> , Type id
  private void parseFormalList()
  {
    if (current.kind == Kind.TOKEN_INT || current.kind == Kind.TOKEN_BOOLEAN
        || current.kind == Kind.TOKEN_ID) {
      parseType();
      eatToken(Kind.TOKEN_ID);
      while (current.kind == Kind.TOKEN_COMMER) {
        advance();
        parseType();
        eatToken(Kind.TOKEN_ID);
      }
    }
    return;
  }

  // Method -> public Type id ( FormalList )
  // { VarDecl* Statement* return Exp ;}
  
  LinkedList<ast.dec.T> formals=new LinkedList<ast.dec.T>();
  LinkedList<ast.dec.T> locals=new LinkedList<ast.dec.T>();
	LinkedList<ast.stm.T> stmsmethod=new LinkedList<ast.stm.T>();
  private ast.method.T parseMethod()
  {
	  formals=new LinkedList<ast.dec.T>();
	  stmsmethod=new LinkedList<ast.stm.T>();
	int ismain=0;
	ast.type.T retType=null,tmp_type=null;
	String lexmer=null,tmp_id=null;

	ast.exp.T retExp=null;;
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a method.
    //new util.Todo();
	
	//System.out.println("__here__error?");
	//public static void main(String[] args)
	//public static int main(String[] args)
	eatToken(Kind.TOKEN_PUBLIC);
	if(current.kind==Kind.TOKEN_STATIC)
	{
		eatToken(Kind.TOKEN_STATIC);
	}	
	
	if(current.kind==Kind.TOKEN_VOID)
	{
		eatToken(Kind.TOKEN_VOID,"?? 530 void type");
		
	}else 
	{
		retType=parseType();
	}
	
	if(current.kind==Kind.TOKEN_MAIN)
	{
		ismain=1;
		eatToken(Kind.TOKEN_MAIN);
	}else if(current.kind==Kind.TOKEN_ID)
	{
		lexmer=current.lexeme;
		eatToken(Kind.TOKEN_ID);
	}else error();
	
	
	eatToken(Kind.TOKEN_LPAREN);
	if(1==ismain)
	{
		eatToken(Kind.TOKEN_STRING);
		//public static void main(String[] args)
		//public static void main(String args[])
		if(current.kind==Kind.TOKEN_ARR)
		{
			eatToken(Kind.TOKEN_ARR);
			mainarg=current.lexeme;
//			System.out.println("hello zat  "+mainarg);
			eatToken(Kind.TOKEN_ID);
		}else if(current.kind==Kind.TOKEN_ID)
		{
			mainarg=current.lexeme;
			eatToken(Kind.TOKEN_ID);
			eatToken(Kind.TOKEN_ARR);
		}else error();
		
	}else do
	{
		if(current.kind==Kind.TOKEN_RPAREN)
			break;
		if(current.kind==Kind.TOKEN_COMMER)
		{
			eatToken(Kind.TOKEN_COMMER);
		}
		
		tmp_type=parseType();
		tmp_id=current.lexeme;
		eatToken(Kind.TOKEN_ID);
		
		formals.add(new ast.dec.Dec(tmp_type, tmp_id));

	}while(current.kind==Kind.TOKEN_COMMER);
	
	eatToken(Kind.TOKEN_RPAREN);
	eatToken(Kind.TOKEN_LBRACE);
	
	//System.out.println("___end__?");
	//Tree   root  ;
	//root   =     new Tree();
	
	
	
	locals=parseVarDecls();
	stmsmethod=parseStatements();

	
	
	
	if(current.kind==Kind.TOKEN_RETURN)
	{
		eatToken(Kind.TOKEN_RETURN);
		retExp=parseExp();
		eatToken(Kind.TOKEN_SEMI);
	}
	
	//eatToken(Kind.TOKEN_RBRACE);
	//mainstm=stms.getFirst();
    return new ast.method.Method(retType, lexmer, formals, locals, stmsmethod, retExp);
  }

  // MethodDecls -> MethodDecl MethodDecls
  // ->
  LinkedList<ast.method.T> listmethod;
  private LinkedList<ast.method.T> parseMethodDecls()
  {
	  listmethod=new LinkedList<ast.method.T>();
	
    while (current.kind == Kind.TOKEN_PUBLIC) {
    	listmethod.add(parseMethod());
    	
    	while(current.kind==Kind.TOKEN_RBRACE)
    		eatToken(Kind.TOKEN_RBRACE);
    	//parseMethod();
    }
    return listmethod;
  }

  // ClassDecl -> class id { VarDecl* MethodDecl* }
  // -> class id extends id { VarDecl* MethodDecl* }
  private ast.classs.Class parseClassDecl()
  {  
//	tmp=new ast.classs.Class(id, extendss, decs, methods)  
    eatToken(Kind.TOKEN_CLASS);
    classid=current.lexeme;
    eatToken(Kind.TOKEN_ID,"543 error");
    if (current.kind == Kind.TOKEN_EXTENDS) {
      eatToken(Kind.TOKEN_EXTENDS);
      classextends=current.lexeme;
      eatToken(Kind.TOKEN_ID);
    }

    eatToken(Kind.TOKEN_LBRACE);
    /*  hand the vars at the beg of the source file*/
    classdecs=parseVarDecls();
    /* hand all of the methods in the source file */
    classmethods=parseMethodDecls();
   // eatToken(Kind.TOKEN_RBRACE);
    return new ast.classs.Class(classid, classextends, classdecs, classmethods);
  }

  // ClassDecls -> ClassDecl ClassDecls
  // ->
	
  private LinkedList<ast.classs.T> parseClassDecls()
  {
	LinkedList<ast.classs.T> list=new LinkedList<ast.classs.T>();  
	  
	 /*hand all of the classes in the sourcefile*/ 
    while (current.kind == Kind.TOKEN_CLASS) {
      list.add(parseClassDecl());
    }
    return list;
  }

  // MainClass -> class id
  // {
  // public static void main ( String [] id )
  // {
  // Statement
  // }
  // }
  private ast.mainClass.MainClass parseMainClass()
  {
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a main class as described by the
    // grammar above.
    //new util.Todo();
	
	  
	if(current.kind == Kind.TOKEN_PUBLIC)
	{
		eatToken(Kind.TOKEN_PUBLIC);
	}
	
	eatToken(Kind.TOKEN_CLASS);
	mainid=current.lexeme;
//	System.out.println("hello+"+id);
	eatToken(Kind.TOKEN_ID);
	eatToken(Kind.TOKEN_LBRACE);
//	mainstm=
	parseMethod();
    
    //eatToken(Kind.TOKEN_RBRACE);
    return new ast.mainClass.MainClass(mainid, mainarg,mainstm);
  }
  
 
  // Program -> MainClass ClassDecl* ast.prog
  private ast.program.Program parseProgram()
  {
    //ast.main.T
	mainclass =parseMainClass();
    classes   =parseClassDecls();
    eatToken(Kind.TOKEN_EOF);
    return new ast.program.Program(mainclass, classes);
  }

  public ast.program.T parse()
  {
	  ast.program.T x =parseProgram();
	  return x;
  }
}
