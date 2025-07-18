package edu.appstate.cs.examples;

public class BadNamesTest {
    
    // declarations with bad names
    private String foo;
    private int x;
    private String bar;
    private String temp;
    private String veryLongVariableNameThatExceedsFiftyCharactersInLength;
    private String _privateField;
    private String field__; // underscores
    private String fielddddd; // repeated chars
    private String ALLUPPERCASE; // not constant
    
    // constants allowed to be uppercase
    private static final String VALID = "OK";
    private static final int MAX_SIZE = 100;
    
    // bad name method with bad parameters
    public void foo(String s, int x, String temp) {
        // Local variables with bad names
        String bar = "test"; 
        int y = 5; 
        String baz = "another test"; 
        
        // single letter counter accepted
        for (int i = 0; i < 10; i++) { 
            System.out.println(i);
        }
        
        // single letter counter denied
        for (int z = 0; z < 10; z++) {
            System.out.println(z);
        }
    }
    
    // good name method
    public void processUserInput(String userInput, int maxLength) {
        // valid var names
        String processedInput = userInput.trim();
        int inputLength = processedInput.length();
        boolean isValid = inputLength <= maxLength;
        
        if (isValid) {
            System.out.println("Valid input: " + processedInput);
        }
    }
    
    public static void main(String[] args) {
        BadNamesTest example = new BadNamesTest();
        example.foo("test", 5, "temporary");
        example.processUserInput("Hello World", 50);
    }
}
