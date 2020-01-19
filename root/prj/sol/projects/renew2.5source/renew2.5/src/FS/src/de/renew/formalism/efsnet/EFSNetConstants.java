package de.renew.formalism.efsnet;

import de.uni_hamburg.fs.Name;
import de.uni_hamburg.fs.Path;


public interface EFSNetConstants {
    public static Name FEATm = new Name("m");
    public static Name FEATpre = new Name("pre");
    public static Path PATHpre = new Path(FEATpre);
    public static Name FEATpost = new Name("post");
    public static Path PATHpost = new Path(FEATpost);
    public static Name FEATpostc = new Name("postc");
    public static Path PATHpostc = new Path(FEATpostc);
    public static Name FEATproc = new Name("proc");
    public static Path PATHproc = new Path(FEATproc);
}