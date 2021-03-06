package parser;

import lexer.Lexer;
import lexer.Token;
import lexer.Token.Kind;

public class Parser
{
  Lexer lexer;
  Token current;
  String fname;

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
  private void parseExpList()
  {
    if (current.kind == Kind.TOKEN_RPAREN)
      return;
    parseExp();
    while (current.kind == Kind.TOKEN_COMMER) {
      advance();
      parseExp();
    }
    return;
  }

  // AtomExp -> (exp)
  // -> INTEGER_LITERAL
  // -> true
  // -> false
  // -> this
  // -> id
  // -> new int [exp]
  // -> new id ()
  private void parseAtomExp()
  {
//  	  System.out.println(current.kind);
    switch (current.kind) {
    case TOKEN_LPAREN:
      advance();
      parseExp();
      eatToken(Kind.TOKEN_RPAREN);
      return;
    case TOKEN_NUM:
//    	System.out.println("here?");
      advance();
      return;
    case TOKEN_TRUE:
      advance();
      return;
    case TOKEN_FALSE://TMD �ӻ�������
        advance();
        return;
    case TOKEN_THIS:
      advance();
      return;
    case TOKEN_ID:
      advance();
      return;
    case TOKEN_NEW: {
      advance();
      switch (current.kind) {
      case TOKEN_INT:
        advance();
        eatToken(Kind.TOKEN_LBRACK);
        parseExp();
        eatToken(Kind.TOKEN_RBRACK);
        return;
      case TOKEN_ID:
        advance();
        eatToken(Kind.TOKEN_LPAREN);
        eatToken(Kind.TOKEN_RPAREN);
        return;
      default:
        error();
        return;
      }
    }
    default:
      error();
      return;
    }
  }

  // NotExp -> AtomExp
  // -> AtomExp .id (expList)
  // -> AtomExp [exp]
  // -> AtomExp .length
  private void parseNotExp()
  {

    parseAtomExp();
    while (current.kind == Kind.TOKEN_DOT || current.kind == Kind.TOKEN_LBRACK) {
      if (current.kind == Kind.TOKEN_DOT) {
        advance();
        if (current.kind == Kind.TOKEN_LENGTH) {
          advance();
          return;
        }
//        System.out.println("here?");
        eatToken(Kind.TOKEN_ID);
        eatToken(Kind.TOKEN_LPAREN);
        parseExpList();
        eatToken(Kind.TOKEN_RPAREN);
      } else {
//    	  System.out.println("here?");
        advance();
        parseExp();
        eatToken(Kind.TOKEN_RBRACK);
      }
    }
    return;
  }

  // TimesExp -> ! TimesExp
  // -> NotExp
  private void parseTimesExp()
  {
    while (current.kind == Kind.TOKEN_NOT) {
      advance();
    }
    parseNotExp();
    return;
  }

  // AddSubExp -> TimesExp * TimesExp
  // -> TimesExp
  private void parseAddSubExp()
  {
    parseTimesExp();
    while (current.kind == Kind.TOKEN_TIMES) {
      advance();
      parseTimesExp();
    }
    return;
  }

  // LtExp -> AddSubExp + AddSubExp
  // -> AddSubExp - AddSubExp
  // -> AddSubExp
  private void parseLtExp()
  {
    parseAddSubExp();
    while (current.kind == Kind.TOKEN_ADD || current.kind == Kind.TOKEN_SUB) {
      advance();
      parseAddSubExp();
    }
    return;
  }

  // AndExp -> LtExp < LtExp
  // -> LtExp
  private void parseAndExp()
  {
    parseLtExp();
    while (current.kind == Kind.TOKEN_LT) {
      advance();
      parseLtExp();
    }
    return;
  }

  // Exp -> AndExp && AndExp
  // -> AndExp
  private void parseExp()
  {
    parseAndExp();
    while (current.kind == Kind.TOKEN_AND) {
      advance();
      parseAndExp();
    }
    return;
  }

  // Statement -> { Statement* }
  // -> if ( Exp ) Statement else Statement
  // -> while ( Exp ) Statement
  // -> System.out.println ( Exp ) ;
  // -> id = Exp ;
  // -> id [ Exp ]= Exp ;
  private void parseStatement()
  {
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a statement.
    //new util.Todo();

	//parseVarDecls();
	while(current.kind==Kind.TOKEN_LBRACE)
	{
		eatToken(Kind.TOKEN_LBRACE);
		parseStatement();
		eatToken(Kind.TOKEN_RBRACE);
	}
	
	while(current.kind==Kind.TOKEN_IF)
	{
		eatToken(Kind.TOKEN_IF);
		eatToken(Kind.TOKEN_LPAREN);
		parseExp();
		eatToken(Kind.TOKEN_RPAREN);
//System.out.println("__if__branch");		
		parseStatement();
		eatToken(Kind.TOKEN_ELSE);
		parseStatement();
	}
	while(current.kind==Kind.TOKEN_WHILE)
	{
		eatToken(Kind.TOKEN_WHILE);
		eatToken(Kind.TOKEN_LPAREN);
		parseExp();
		eatToken(Kind.TOKEN_RPAREN);
		parseStatement();
	}
	while(current.kind==Kind.TOKEN_SYSTEM)
	{
		eatToken(Kind.TOKEN_SYSTEM);
		eatToken(Kind.TOKEN_DOT);
		eatToken(Kind.TOKEN_OUT);
		eatToken(Kind.TOKEN_DOT);
		eatToken(Kind.TOKEN_PRINTLN);
		eatToken(Kind.TOKEN_LPAREN);
		parseExp();
		eatToken(Kind.TOKEN_RPAREN);
		eatToken(Kind.TOKEN_SEMI);
	}
	while(current.kind==Kind.TOKEN_ID)
	{
//		System.out.println("__zat log__here__");
		eatToken(Kind.TOKEN_ID);
		
		if(current.kind==Kind.TOKEN_ASSIGN)
		{
			eatToken(Kind.TOKEN_ASSIGN);
			parseExp();
			eatToken(Kind.TOKEN_SEMI);
			parseStatement();
		}
		// Statement -> { Statement* }
		  // -> if ( Exp ) Statement else Statement
		  // -> while ( Exp ) Statement
		  // -> System.out.println ( Exp ) ;
		  // -> id = Exp ;
		  // -> id [ Exp ]= Exp ;
		else{
			eatToken(Kind.TOKEN_LBRACK);
			parseExp();
			eatToken(Kind.TOKEN_RBRACK);
			eatToken(Kind.TOKEN_ASSIGN);
			parseExp();
			eatToken(Kind.TOKEN_SEMI);
			parseStatement();
		}
	}
//	 System.out.println("error here line 295");
	 return; 
  }

  // Statements -> Statement Statements
  // ->
  private void parseStatements()
  {
    while (current.kind == Kind.TOKEN_LBRACE || current.kind == Kind.TOKEN_IF
        || current.kind == Kind.TOKEN_WHILE
        || current.kind == Kind.TOKEN_SYSTEM || current.kind == Kind.TOKEN_ID) {
      parseStatement();
    }
    return;
  }

  // Type -> int []
  // -> boolean
  // -> int
  // -> id
  private void parseType()
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
			return;
		}
		
		return;
		
	}else if(current.kind==Kind.TOKEN_BOOLEAN)
	{
		eatToken(Kind.TOKEN_BOOLEAN);
		return;
		
	}else if(current.kind==Kind.TOKEN_ID)
	{
		eatToken(Kind.TOKEN_ID);
		if(current.kind==Kind.TOKEN_LBRACK)
		{
			eatToken(Kind.TOKEN_LBRACK);
			eatToken(Kind.TOKEN_NUM);
			eatToken(Kind.TOKEN_RBRACK);
		}
		return ;
		
	}
	
	error();
	  
  }

  // VarDecl -> Type id ;
  private void parseVarDecl()
  {
    // to parse the "Type" nonterminal in this method, instead of writing
    // a fresh one.
    parseType();
    if(current.kind==Kind.TOKEN_ASSIGN)
    {
    	eatToken(Kind.TOKEN_ASSIGN);
    	parseExp();
//    	System.out.println("__here?__");
    	eatToken(Kind.TOKEN_SEMI);
    	return;
    }
    	
    eatToken(Kind.TOKEN_ID,"here error?  373");
    eatToken(Kind.TOKEN_SEMI);
    return;
  }

  // VarDecls -> VarDecl VarDecls
  // ->
  private void parseVarDecls()
  {
    while (current.kind == Kind.TOKEN_INT || current.kind == Kind.TOKEN_BOOLEAN
        || current.kind == Kind.TOKEN_ID) {
      parseVarDecl();
    }
    return;
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
  private void parseMethod()
  {
	int ismain=0;  
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
		eatToken(Kind.TOKEN_VOID);
	}else 
	{
		parseType();
	}
	
	if(current.kind==Kind.TOKEN_MAIN)
	{
		ismain=1;
		eatToken(Kind.TOKEN_MAIN);
	}else if(current.kind==Kind.TOKEN_ID)
	{
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
			eatToken(Kind.TOKEN_ID);
		}else if(current.kind==Kind.TOKEN_ID)
		{
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
		
		parseType();
		eatToken(Kind.TOKEN_ID);
		

	}while(current.kind==Kind.TOKEN_COMMER);
	
	eatToken(Kind.TOKEN_RPAREN);
	eatToken(Kind.TOKEN_LBRACE);
	

	parseVarDecls();
//	System.out.println("___end__?");
	//public static int main(String[] args)
	/* {
	 * 
	 * 		Statements;
	 * 
	 * 
	 * }
	 */ 
	parseStatement();
	
	if(current.kind==Kind.TOKEN_RETURN)
	{
		eatToken(Kind.TOKEN_RETURN);
		parseExp();
		eatToken(Kind.TOKEN_SEMI);
	}
	
	eatToken(Kind.TOKEN_RBRACE);
    return;
  }

  // MethodDecls -> MethodDecl MethodDecls
  // ->
  private void parseMethodDecls()
  {
    while (current.kind == Kind.TOKEN_PUBLIC) {
      parseMethod();
    }
    return;
  }

  // ClassDecl -> class id { VarDecl* MethodDecl* }
  // -> class id extends id { VarDecl* MethodDecl* }
  private void parseClassDecl()
  {
    eatToken(Kind.TOKEN_CLASS);
    eatToken(Kind.TOKEN_ID);
    if (current.kind == Kind.TOKEN_EXTENDS) {
      eatToken(Kind.TOKEN_EXTENDS);
      eatToken(Kind.TOKEN_ID);
    }
    eatToken(Kind.TOKEN_LBRACE);
    /*  hand the vars at the beg of the source file*/
    parseVarDecls();
    /* hand all of the methods in the source file */
    parseMethodDecls();
    eatToken(Kind.TOKEN_RBRACE);
    return;
  }

  // ClassDecls -> ClassDecl ClassDecls
  // ->
  private void parseClassDecls()
  {
	 /*hand all of the classes in the sourcefile*/ 
    while (current.kind == Kind.TOKEN_CLASS) {
      parseClassDecl();
    }
    return;
  }

  // MainClass -> class id
  // {
  // public static void main ( String [] id )
  // {
  // Statement
  // }
  // }
  private void parseMainClass()
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
	eatToken(Kind.TOKEN_ID);
	eatToken(Kind.TOKEN_LBRACE);
	
    parseMethod();
    
    eatToken(Kind.TOKEN_RBRACE);

  }

  // Program -> MainClass ClassDecl*
  private void parseProgram()
  {
    parseMainClass();
    parseClassDecls();
    eatToken(Kind.TOKEN_EOF);
    return;
  }

  public void parse()
  {
    parseProgram();
    return;
  }
}
