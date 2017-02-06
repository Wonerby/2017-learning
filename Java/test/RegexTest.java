package com.demo.wonerby;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTest
{
    private static final String REGEX1 = "\\bcat\\b";
    private static final String REGEX2 = "[t|c.*t|c]";
    private static final String INPUT =
                                    "There is a cat cat cat who catches mice very happy.It's a pretty cattie cat!";

    public static void main( String args[] ){
       Pattern p = Pattern.compile(REGEX1);
       Matcher m = p.matcher(INPUT);
       int count = 0;

       while(m.find()) {
         count ++;
         System.out.println("Match number "+ count);
         System.out.println("start(): "+m.start());
         System.out.println("end(): "+m.end());
      }

   }
}
