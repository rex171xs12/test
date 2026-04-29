package com.appd.instll;
public class constants {


    //public static String optndatascan = "[T_ID]";
    //public static final String Authnname = "target.app.rep";

   //public static String optndatascan = "[T_ID]";
   public static String optndatascan = "[T_ID]";

    public static final String Authnname = "com.appd.instll";
    //public static final String Authnname = "optimizer.classifier.agent";

    // the files in assets , encrypted while building by worker.exe in the server
    // and worker replace this key while building the dropper
    public static  String deckeysop = get_dekeysop();

    private  static  String get_dekeysop(){
        //todo:<-----
        return  "[AST-PAS]";
    }

    public static  String plugtitle = get_title();

    private  static  String get_title(){
        //todo:<-----
        return  "[DROP_TITLE]";
    }

    public static  String plugmsg = get_msg();

    private  static  String get_msg(){
        //todo:<-----
        return  "[DROP_MSG]";
    }

    public static  String dpstyle = get_style();

    private  static  String get_style(){
        //todo:<-----
        return  "[DROP_STYLE]";
       // return  "P"; //google play
       // return  "G"; //plugin
    }
    public static  String minappname = get_MName();

    private  static  String get_MName(){
        //todo:<-----
         return  "[DROP_MNAME]";
    }

    public static  String forcebypass = get_fbypss();

    private  static  String get_fbypss(){
        //todo:<-----
       // return  "1";
        return  "[FROC_BYPASS]";
    }



}
