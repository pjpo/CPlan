grammar Expr;

@header {
	import java.util.LinkedList;
	import com.github.pjpo.planning.jours.ChocoRule;
	import java.util.List;
}

file: (equality | difference)* EOF;

equality :
  EQ PARI rules=rulei PARO;
difference:
  NOT EQ PARI rules=rulei PARO;

rulei returns [List<ChocoRule> rules]
  @init {
  	List<ChocoRule> chocoRules = new LinkedList<ChocoRule>();
  }
  @after {
  	$rules = chocoRules;
  }:
  first=rule_element {chocoRules.add($first.rule);}
  (COMMA next=rule_element {chocoRules.add($next.rule);})*;
rule_element returns [ChocoRule rule]:
  name=(WORD | NUMBER)+ value=index {
  	$rule = new ChocoRule();
  	$rule.setName($name.text);
  	$rule.setValue($value.n);
  };

index returns [Integer n]:
  BRACKETI (valuepos=indexpos | valueneg=indexneg)? BRACKETO {
  	if ($valuepos.text != null) {
  		$n = $valuepos.n;
  	} else if ($valueneg.text != null) {
  		$n = $valueneg.n;
  	} else {
  		$n = 0;
  	}
  };
indexpos returns [Integer n]:
  PLUS num=NUMBER {
  	$n = Integer.parseInt($num.text);
  };
indexneg returns [Integer n]:
   MINUS num=NUMBER {
  	$n = Integer.parseInt($num.text);
  };


WORD : ('a'..'z' | 'A'..'Z' | '_')+;
// Start parenthesis
PARI : '(';
//End parenthesis
PARO : ')';
// Equality
EQ : '=';
// Not
NOT : '!';
// Start comment
COMMENT : ('%%' ~('\r' | '\n')*) -> skip;
// Number
NUMBER  : [0-9]+;
// Comma
COMMA : ',';
// Minus
MINUS : '-';
// Plus
PLUS : '+';
// Start bracket
BRACKETI : '[n';
// End bracket
BRACKETO : ']';
// Newlines after comments 
NEWLINE : (('\r'? '\n') | '\r') -> skip;
// White spaces have no signification
WS : [' '\t]+ -> skip ;