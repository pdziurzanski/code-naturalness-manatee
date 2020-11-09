package uk.ac.cs.york.manatee;


 
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class ParseAST {

	@SuppressWarnings("deprecation")
	public CompilationUnit parse(String source) {
		ASTParser parser = ASTParser.newParser(AST.JLS11);
		parser.setSource(source.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
 		
		return (CompilationUnit) parser.createAST(null);
	}
	
	
	
	public String abstractiseTokens(String source){
		
		final CompilationUnit cu=parse(source);
		
		final ASTRewrite rewrite = ASTRewrite.create(cu.getAST());
		
		
		cu.accept(new ASTVisitor() {
 
 
			public boolean visit(StringLiteral node) {
				node.setLiteralValue("StringLiteral");
				return false; // do not continue 
			}


			public boolean visit(NumberLiteral node) {
				node.setToken("1");
				return false; // do not continue 
			}
			
			

			public boolean visit(CharacterLiteral node) {
				node.setCharValue('a');
				return false;
			}

		
			public boolean visit(SimpleType node) {
				node.setStructuralProperty(SimpleType.NAME_PROPERTY, 
						rewrite.getAST().newSimpleName("SimpleType"));
				return false;
			}			
			
			
			
			public boolean visit(LineComment node) {
				node.delete();
				return false;
				
			}

			public boolean visit(BlockComment node) {
				node.delete();
				return false;
				
			}

			public boolean visit(Javadoc node) {
				node.delete();
				return false;
			}

		     
			public boolean visit(SimpleName node) {
				node.setIdentifier("SimpleName");
				
				return true;
			}

		     


		});
		

		return cu.toString();
	}
}
