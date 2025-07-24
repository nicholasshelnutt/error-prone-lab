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

    // var reassigned
    public void reassignVar()
    {
        String val = "Initial";
        System.out.println(val); // no warning
        val = "Reassigned";
        System.out.println(val); // no warning
    }

    // expression instantly returned
    public int expressionInstantReturn()
    {
        int calculation = 5 * 10 + 15;
        return calculation; // triggers a warning
    }

    // variable used in return but not instantly
    public String notInstantReturn()
    {
        String prefix = "Nick ";
        String suffix = "Shelnutt";
        return prefix + suffix; // no warning
    }

    // loop var
    public void loopVariable()
    {
        for (int i = 0; i < 10; i++)
        {
            String thing = "Thing " + i;
            System.out.println(thing); // no warning
        }
    }

    public static void main(String[] args) 
    {
        BadLocalVarTest test = new BadLocalVarTest();
        System.out.println(test.getInstantReturn());
        test.singleUseVariable();
        test.multipleUseVariable();
        test.reassignVar();
        System.out.println(test.expressionInstantReturn());
        System.out.println(test.notInstantReturn());
        test.loopVariable();
    }
}
