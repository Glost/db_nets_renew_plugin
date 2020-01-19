#!/usr/bin/nawk
BEGIN {
    waitpar=1;
    indented=0;
}
function output(s) {
    if (s=="") {
	if (waitpar==0) {
	    print s;
	    waitpar=1;
	}
    } else {
	gsub("^[[:space:]]*","",s);
	if (indented>=1) {
	    for (i=1;i<=indented;i++) {
		s="  " s;
	    }    
	}
	print s;
	waitpar=0;
    }
}
function untex(s) {
    gsub("\\\\[a-zA-Z@]*","",s);
    gsub("\\\\ss ","",s);
    gsub("\\\\a","ä",s);
    gsub("\\\\o","ö",s);
    gsub("\\\\u","ü",s);
    gsub("\\\\A","Ä",s);
    gsub("\\\\O","Ö",s);
    gsub("\\\\U","Ü",s);
    gsub("{","",s);
    gsub("}","",s);
    return s;
}
function printheader(s,c) {
    # Make sure to terminate paragraph.
    output("");
    # Output two blank lines.
    print "";
    # Format header
    sub("[^{]*{","",s);
    sub("}[^}]*","",s);
    s=untex(s);
    output(s);
    # Emphasize.
    t="";
    for (i=1;i<=length(s);i++) {
	t=t c;
    }
    output(t);
    # Make sure to terminate paragraph.
    output("");
}
/READMEIGNORE/ {next;}
/\\chapter/ {
    printheader($0,"=");
    next;
}
/\\section/ {
    printheader($0,"-");
    next;
}
/\\subsection/ {
    printheader($0,".");
    next;
}
/\\begin{licensequote}/ {
    indented=1;
    output("");
    next;
}
/\\end{licensequote}/ {
    indented=0;
    output("");
    next;
}
/\\begin{itemize}/ {
    indented++;
    output("");
    next;
}
/\\end{itemize}/ {
    indented--;
    output("");
    next;
}
/\\item/ {
    sub("\\\\item[[:space:]]*\\[","",$0);
    sub("\\]","",$0);
    indented--;
    output($0);
    indented++;
    next;
}
{
    output(untex($0));
}


