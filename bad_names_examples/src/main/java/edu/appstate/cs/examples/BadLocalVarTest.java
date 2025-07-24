package edu.appstate.cs.examples;

public class BadLocalVarTest 
{
    // var instant returned
    public String getInstantReturn()
    {
        String result = "Hello, World!";
        return result; // triggers a warning
    }

    // variable only used once
    public void singleUseVariable()
    {
        String result = "Hello, World!";
        System.out.println(result); // triggers a warning
    }

    // variable used multiple timnes
    public void multipleUseVariable()
    {
        String result = "Hello, World!";
        System.out.println(result);
        System.out.println(result); // no warning
    }
}
