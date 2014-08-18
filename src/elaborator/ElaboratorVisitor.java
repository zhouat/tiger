package elaborator;

public class ElaboratorVisitor implements ast.Visitor
{
  public ClassTable classTable; // symbol table for class
  public MethodTable methodTable; // symbol table for each method
  public String currentClass; // the class name being elaborated
  public String currentMethod;
  public ast.type.T type; // type of the expression being elaborated

  public ElaboratorVisitor()
  {
    this.classTable = new ClassTable();
    this.methodTable = new MethodTable();
    this.currentClass = null;
    this.type = null;
  }

  private void error()
  {
    System.out.println("type mismatch");
    System.exit(1);
  }
  
  private void error(String str)
  {
    System.out.println(str);
//    System.exit(1);
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
 
  public void visit(ast.exp.Add e)
  {
	  //System.out.println("ast.exp.Add");  
	  e.left.accept(this);
	  ast.type.T ty=this.type;
	  e.right.accept(this);
	  if(!this.type.toString().equals(ty.toString()))
		  error("Add's args type is not legal ["+ty.toString()+" + "+this.type.toString()+" ]");
	  this.type= new ast.type.Int();
	  return;
  }

  @Override
  public void visit(ast.exp.And e)
  {
	  //System.out.println("ast.exp.And");
	  e.left.accept(this);
	  ast.type.T ty=this.type;
	  e.right.accept(this);
	  if(!this.type.toString().equals(ty.toString()))
		  error("line 52");
	  this.type= new ast.type.Boolean();
	  return;
  }

  @Override
  public void visit(ast.exp.ArraySelect e)
  {
	  //System.out.println("ast.exp.ArraySelect");
	  e.array.accept(this);
	  ast.type.T ty=this.type;
	  e.index.accept(this);
	  if(!this.type.toString().equals((new ast.type.Int()).toString()))
		  error("ArraySelect's subscript is not int");
	  
	  this.type= new ast.type.Int();
//	  System.out.println(this.type+"=====");
	  return;
  }

  @Override
  public void visit(ast.exp.Call e)
  {
	  //System.out.println("ast.exp.Call");
    ast.type.T leftty;
    ast.type.Class ty = null;

    e.exp.accept(this);
    leftty = this.type;
    if (leftty instanceof ast.type.Class) {
      ty = (ast.type.Class) leftty;
      e.type = ty.id;
    } else
      error("call fun's is not belong to any class ["+e.id+" ]");
    MethodType mty = this.classTable.getm(ty.id, e.id);
    java.util.LinkedList<ast.type.T> argsty = new java.util.LinkedList<ast.type.T>();

    if(e.args!=null)
    for (ast.exp.T a : e.args) {
      a.accept(this);
      argsty.addLast(this.type);
    }
    if (mty.argsType.size() != argsty.size())
      error("line 96");
    for (int i = 0; i < argsty.size(); i++) {
      ast.dec.Dec dec = (ast.dec.Dec) mty.argsType.get(i);
      if (dec.type.toString().equals(argsty.get(i).toString()))
        ;
      else
        error("the symbol try to add to symbol table failed: "+dec.id);
    }
    this.type = mty.retType;
    e.at = argsty;
    e.rt = this.type;
    return;
  }

  @Override
  public void visit(ast.exp.False e)
  {
	  //System.out.println("ast.exp.False");
	  this.type= new ast.type.Boolean();
	  return;
  }

  @Override
  public void visit(ast.exp.Id e)
  {
	//System.out.println("ast.exp.Id "+e.id.toString());  
    // first look up the id in method table
    ast.type.T type = this.methodTable.get(e.id);
    // if search failed, then s.id must be a class field.
    if (type == null) {
      type = this.classTable.get(this.currentClass, e.id);
      // mark this id as a field id, this fact will be
      // useful in later phase.
      e.isField = true;
    }
    if (type == null)
      error("symbol table not contain  " + e.id);
    this.type = type;
    // record this type on this node for future use.
    e.type = type;
    return;
  }

  @Override
  public void visit(ast.exp.Length e)
  {
	  //System.out.println("ast.exp.Length");
	  e.array.accept(this);
	  ast.type.T ty=this.type;
	  
	  this.type= new ast.type.Int();
	  return;
  }

  @Override
  public void visit(ast.exp.Lt e)
  {
	//System.out.println("ast.exp.Lt");
    e.left.accept(this);
    ast.type.T ty = this.type;
    e.right.accept(this);
    if (!this.type.toString().equals(ty.toString()))
      error("lt's args type is not legal ["+ty.toString()+"<"+this.type.toString()+" ]");
    this.type = new ast.type.Boolean();
    return;
  }

  @Override
  public void visit(ast.exp.NewIntArray e)
  {
	  //System.out.println("ast.exp.NewIntArray");
	  this.type=new ast.type.IntArray();
	  return;
	  
  }

  @Override
  public void visit(ast.exp.NewObject e)
  {
	//System.out.println("ast.exp.NewObject");
    this.type = new ast.type.Class(e.id);
    return;
  }

  @Override
  public void visit(ast.exp.Not e)
  {
	 //System.out.println("ast.exp.Not "+e.exp);
	 e.exp.accept(this);
	 ast.type.T ty=this.type;
	 if(!ty.toString().equals((new ast.type.Boolean()).toString()))
	 {
		 error("the type is not boolean ["+e.exp+" ]");
	 }
	 return;
  }

  @Override
  public void visit(ast.exp.Num e)
  {
	//System.out.println("ast.exp.Num "+e.num);	
    this.type = new ast.type.Int();
    return;
  }

  @Override
  public void visit(ast.exp.Sub e)
  {
	//System.out.println("ast.exp.Sub");
    e.left.accept(this);
    ast.type.T leftty = this.type;
    e.right.accept(this);
    if (!this.type.toString().equals(leftty.toString()))
      error("Sub's args type is not legal [ "+leftty.toString()+" - "+this.type.toString()+" ]");
    this.type = new ast.type.Int();
    return;
  }

  @Override
  public void visit(ast.exp.This e)
  {
	//System.out.println("ast.exp.This");
    this.type = new ast.type.Class(this.currentClass);
    return;
  }

  @Override
  public void visit(ast.exp.Times e)
  {
	  //System.out.println("ast.exp.Times");
    e.left.accept(this);
    ast.type.T leftty = this.type;
    e.right.accept(this);
    if (!this.type.toString().equals(leftty.toString()))
        error("Times's args type is not legal [ "+e.left.toString()+leftty.toString()+" * "+this.type.toString()+" ]");
    this.type = new ast.type.Int();
    return;
  }

  @Override
  public void visit(ast.exp.True e)
  {
	  //System.out.println("ast.exp.True");
	  this.type =new ast.type.Boolean(); 
	  return;
  }

  // statements
  @Override
  public void visit(ast.stm.Assign s)
  {
	//System.out.println("ast.stm.Assign");
    // first look up the id in method table
    ast.type.T type = this.methodTable.get(s.id);
    // if search failed, then s.id must
    if (type == null)
      type = this.classTable.get(this.currentClass, s.id);
    if (type == null)
      error();
    s.exp.accept(this);
    s.type = type;
    if(!this.type.toString().equals(type.toString()))
    {
    	error("assign type not legal: "+s.id+" = "+this.type.toString());
    }
    return;
  }

  @Override
  public void visit(ast.stm.AssignArray s)
  {
	  //System.out.println("ast.stm.AssignArray");
  }

  @Override
  public void visit(ast.stm.Block s)
  {
	 //System.out.println("ast.stm.Block");
  }

  @Override
  public void visit(ast.stm.If s)
  {
	  //System.out.println("ast.stm.If");
    s.condition.accept(this);
    if (!this.type.toString().equals("@boolean"))
      error("If's condition type is not boolean");
    s.thenn.accept(this);
    s.elsee.accept(this);
    return;
  }

  @Override
  public void visit(ast.stm.Print s)
  {
	  //System.out.println("ast.stm.Print");
    s.exp.accept(this);
    if (!this.type.toString().equals("@int"))
      error("print() fun's arg is not int");
    return;
  }

  @Override
  public void visit(ast.stm.While s)
  {
	 //System.out.println("ast.stm.While");
	 s.condition.accept(this);
	 ast.type.T ty=this.type;
	 if(!this.type.toString().equals("@boolean"))
		error("while condition is not boolean ["+s.condition.toString()+"]");
	 s.body.accept(this);
  }

  // type
  @Override
  public void visit(ast.type.Boolean t)
  {
	  //System.out.println("ast.type.Boolean");
  }

  @Override
  public void visit(ast.type.Class t)
  {
	//System.out.println("ast.type.Class");
  }
  

  @Override
  public void visit(ast.type.Int t)
  {
	//System.out.println("ast.type.Int");  
  }

  @Override
  public void visit(ast.type.IntArray t)
  {
	 //System.out.println("ast.type.IntArray");
  }

  // dec
  @Override
  public void visit(ast.dec.Dec d)
  {
	//System.out.println("ast.dec.Dec");  
  }

  // method
  @Override
  public void visit(ast.method.Method m)
  {
    // construct the method table
	this.currentMethod = m.id.toString();  
	this.methodTable=new MethodTable();
	
    this.methodTable.put(m.formals, m.locals);

    if (control.Control.elabMethodTable)
      this.methodTable.dump();

    for (ast.stm.T s : m.stms)
      s.accept(this);
    m.retExp.accept(this); 
    ast.type.T ty=this.type;
    if(!ty.toString().equals(this.classTable.getm(currentClass, currentMethod).retType.toString()))
    	error("the retType is not legal! ["+this.classTable.getm(currentClass, currentMethod).retType+" = "+ty.toString()+" ]");
    return;
  }

  // class
  @Override
  public void visit(ast.classs.Class c)
  {
    this.currentClass = c.id;

    for (ast.method.T m : c.methods) {
      m.accept(this);
    }
    return;
  }

  // main class
  @Override
  public void visit(ast.mainClass.MainClass c)
  {
    this.currentClass = c.id;
    // "main" has an argument "arg" of type "String[]", but
    // one has no chance to use it. So it's safe to skip it...

    c.stm.accept(this);
    return;
  }

  // ////////////////////////////////////////////////////////
  // step 1: build class table
  // class table for Main class
  private void buildMainClass(ast.mainClass.MainClass main)
  {
    this.classTable.put(main.id, new ClassBinding(null));
    
  }

  // class table for normal classes
  private void buildClass(ast.classs.Class c)
  {
    this.classTable.put(c.id, new ClassBinding(c.extendss));
    for (ast.dec.T dec : c.decs) {
      ast.dec.Dec d = (ast.dec.Dec) dec;
      this.classTable.put(c.id, d.id, d.type);
    }
    
    for (ast.method.T method : c.methods) 
    {    	
      ast.method.Method m = (ast.method.Method) method;
      this.classTable.put(c.id, m.id, new MethodType(m.retType, m.formals));
    }
  }

  // step 1: end
  // ///////////////////////////////////////////////////

  // program
  @Override
  public void visit(ast.program.Program p)
  {
    // ////////////////////////////////////////////////
    // step 1: build a symbol table for class (the class table)
    // a class table is a mapping from class names to class bindings
    // classTable: className -> ClassBinding{extends, fields, methods}
    buildMainClass((ast.mainClass.MainClass) p.mainClass);
    for (ast.classs.T c : p.classes) {
      buildClass((ast.classs.Class) c);
    }

    // we can double check that the class table is OK!
    if (control.Control.elabClassTable) {
      this.classTable.dump();
    }

    // ////////////////////////////////////////////////
    // step 2: elaborate each class in turn, under the class table
    // built above.
    p.mainClass.accept(this);
    for (ast.classs.T c : p.classes) {
      c.accept(this);
    }

  }
}
