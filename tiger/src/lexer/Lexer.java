package lexer;

import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import util.Todo;
import lexer.Token.Kind;

public class Lexer
{
  String fname; // the input file name to be compiled
  InputStream fstream; // input stream for the above file
  PushbackInputStream pstream;
  public static Integer line_num=1;
  public static String info="";
 
  public static Map<Kind,String> Keywords=new HashMap<Kind,String>()
  {
	  {
		  put(Kind.TOKEN_BOOLEAN, "boolean");
		  put(Kind.TOKEN_CLASS,"class");
		  put(Kind.TOKEN_DO,"do");
		  put(Kind.TOKEN_ELSE,"else");
		  put(Kind.TOKEN_EXTENDS,"extends");
		  put(Kind.TOKEN_FALSE,"false");
		  put(Kind.TOKEN_FOR,"for");
		  put(Kind.TOKEN_IF,"if");
		  put(Kind.TOKEN_INT,"int");
		  put(Kind.TOKEN_LENGTH,"length");
		  put(Kind.TOKEN_MAIN,"main");
		  put(Kind.TOKEN_NEW,"new");
		  put(Kind.TOKEN_OUT,"out");
		  put(Kind.TOKEN_PRINT,"print");
		  put(Kind.TOKEN_PRINTLN,"println");
		  put(Kind.TOKEN_PUBLIC,"public");
		  put(Kind.TOKEN_PRIVATE,"private");
		  put(Kind.TOKEN_RETURN,"return");
		  put(Kind.TOKEN_STRING,"String");
		  put(Kind.TOKEN_STATIC,"static");
		  put(Kind.TOKEN_SYSTEM,"System");
		  put(Kind.TOKEN_THEN,"then");
		  put(Kind.TOKEN_THIS,"this");
		  put(Kind.TOKEN_VOID,"void");
		  put(Kind.TOKEN_WHILE,"while");		  
	  }
  };

  
  public Lexer(String fname, InputStream fstream)
  {
    this.fname = fname;
    this.fstream = fstream;
    pstream=new PushbackInputStream(fstream,50);
  }

  // When called, return the next token (refer to the code "Token.java")
  // from the input stream.
  // Return TOKEN_EOF when reaching the end of the input stream.
  private Token nextTokenInternal() throws Exception
  {
	  
    int c = this.pstream.read();
    if (-1 == c)
      // The value for "lineNum" is now "null",
      // you should modify this to an appropriate
      // line number for the "EOF" token.
      return new Token(Kind.TOKEN_EOF, line_num);

    // skip all kinds of "blanks"
    while (' ' == c || '\t' == c || '\n' == c || '\r'==c) {
      if('\n'==c)
      {
    	  line_num++;
      }
      c = this.fstream.read();
    }
    
    System.out.println("c is "+c+"  "+(char)c);
    
    if (-1 == c)
      return new Token(Kind.TOKEN_EOF, line_num);

    switch (c) {
    	/*   hand the symbols  */
	    case '+':	return new Token(Kind.TOKEN_ADD, line_num);
	    case '-':	return new Token(Kind.TOKEN_SUB,line_num);
	    case '*':	return new Token(Kind.TOKEN_TIMES,line_num);
	    case '/':
	    {
	    			if('/'==(c=this.pstream.read()))
	    			{
	    				return new Token(Kind.TOKEN_COMMEN,line_num);
	    			}
	    			
	    			this.pstream.unread(c);
	    			return new Token(Kind.TOKEN_DIV,line_num);
	    }
	    case '&':
	    {
	    			if('&'==(c=this.pstream.read()))
	    			{
	    				return new Token(Kind.TOKEN_AND,line_num);
	    			}
	    			
	    			this.pstream.unread(c);
	    			return null;
	    	
	    }
	    case '[':
	    {
	    			if(']'==(c=this.pstream.read()))
	    			{
	    				return new Token(Kind.TOKEN_ARR,line_num);
	    			}
	    			
	    			this.pstream.unread(c);
	    			return new Token(Kind.TOKEN_LBRACK,line_num);
	    }
	    case ']':	return new Token(Kind.TOKEN_RBRACK,line_num);
	    case '=':
	    {
	    			if('='==(c=this.pstream.read()))
	    			{
	    				return new Token(Kind.TOKEN_EQ,line_num);
	    			}
	    			
	    			this.pstream.unread(c);
	    			return new Token(Kind.TOKEN_ASSIGN,line_num);
	    }
	    case ',':	return new Token(Kind.TOKEN_COMMER,line_num);
	    case '.':	return new Token(Kind.TOKEN_DOT,line_num);
	    case ';':	return new Token(Kind.TOKEN_SEMI,line_num);
	    case '>':{
	    			if('='==(c=this.pstream.read()))
	    			{
	    				return new Token(Kind.TOKEN_GE,line_num);
	    			}
	    			
	    			this.pstream.unread(c);
	    			return new Token(Kind.TOKEN_GT,line_num);
	    }
	    case '<':{
	    			if('='==(c=this.pstream.read()))
	    			{
	    				return new Token(Kind.TOKEN_LE,line_num);
	    			}
	    			
	    			this.pstream.unread(c);
	    			return new Token(Kind.TOKEN_LT,line_num);
	    }
	    case '{':	return new Token(Kind.TOKEN_LBRACE,line_num);
	    case '}':	return new Token(Kind.TOKEN_RBRACE,line_num);	    
	    case '(':	return new Token(Kind.TOKEN_LPAREN,line_num);
	    case ')':	return new Token(Kind.TOKEN_RPAREN,line_num);
	    case '!':{
	    			if('='==(c=this.pstream.read()))
	    			{
	    				return new Token(Kind.TOKEN_NEQ,line_num);
	    			}
	    			
	    			this.pstream.unread(c);
	    			return new Token(Kind.TOKEN_NOT,line_num);
	    }
	    case '|':{
	    			if('|'==(c=this.pstream.read()))
	    			{
	    				return new Token(Kind.TOKEN_OR,line_num);
	    			}
	    			
	    			this.pstream.unread(c);
	    			return null;
	    }

	    
    default:
      // Lab 1, exercise 2: supply missing code to
      // lex other kinds of tokens.
      // Hint: think carefully about the basic
      // data structure and algorithms. The code
      // is not that much and may be less than 50 lines. If you
      // find you are writing a lot of code, you
      // are on the wrong way.
//      new Todo();
//      return null;
    	
    	
	    /*		hand the num	*/
    	info="";
    	while(c>='0'&&c<='9')
    	{
    		info+=(char)c;
    		c=this.pstream.read();
    	}
    	
    	if(!info.equals(""))
    	{
        	this.pstream.unread(c);
        	return new Token(Kind.TOKEN_NUM,line_num,info);	
    	}

    	
    	/*		hand the alpha		*/
    	info=""; 
    	
    	while((c>='a'&&c<='z')||(c>='A'&&c<='Z')||(c>='0'&&c<='9')||'_'==c)
    	{
    		info+=(char)c;
    		c=this.pstream.read();
    	}
    	
    	this.pstream.unread(c);
    	for (Entry<Kind, String> items : Keywords.entrySet()) {
    		
    		if(items.getValue().equals(info))
    		{
//    			System.out.println("zat log:"+c);
    			return new Token(items.getKey(),line_num);	
    		}
    			
		}
    	
    	return new Token(Kind.TOKEN_ID,line_num,info);
    	
    }
  }

  public Token nextToken()
  {
    Token t = null;

    try {
      t = this.nextTokenInternal();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    if (control.Control.lex)
      System.out.println(t.toString());
    return t;
  }
}
