grammar Expr;

file: (equality | difference | comment)* EOF;

equality: '=' comment* '(' concatenation ')';
difference: '!=' comment* '(' concatenation ')';

concatenation:
  comment* content_index comment*
    (',' comment* concatenation)? comment*;
content_index: content (indexneg | indexpos | noindex);
content: (WORD | NUMBER | '_')+;
indexneg: '[n-' NUMBER ']';
indexpos: '[n+' NUMBER ']';
noindex: '[n]';

comment: (('%%' any_except_newline?) | SPACE*) NEWLINE;

any_except_newline:
  (WORD | NUMBER | SPACE | ANYCHAR)+;

WORD    : ('a'..'z' | 'A'..'Z')+;
NUMBER  : ('0'..'9')+;
NEWLINE : '\r'? '\n' | '\r';
SPACE   : (' ' | '\t');
ANYCHAR : .; 