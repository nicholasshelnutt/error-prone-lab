package edu.appstate.cs.checker;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.*;

import javax.lang.model.element.Name;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;

@AutoService(BugChecker.class)
@BugPattern(
        name = "BadNamesChecker",
        summary = "Poor-quality identifiers",
        severity = WARNING,
        linkType = CUSTOM,
        link = "https://github.com/plse-Lab/"
)
public class BadNamesChecker extends BugChecker implements
        BugChecker.IdentifierTreeMatcher,
        BugChecker.MethodInvocationTreeMatcher,
        BugChecker.MethodTreeMatcher, 
        BugChecker.IfTreeMatcher,
        BugChecker.VariableTreeMatcher {

    @java.lang.Override
    public Description matchIdentifier(IdentifierTree identifierTree, VisitorState visitorState) {
        // NOTE: This matches identifier uses. Do we want to match these,
        // or just declarations?
        Name identifier = identifierTree.getName();
        return checkName(identifierTree, identifier);
    }

    @Override
    public Description matchIf(IfTree tree, VisitorState state)
    {
        if (tree.getElseStatement() == null)
        {
            return buildDescription(tree)
                .setMessage("We found an if without an else")
                .build();
        }
        return Description.NO_MATCH;
    }

    @Override
    public Description matchMethodInvocation(MethodInvocationTree methodInvocationTree, VisitorState visitorState) {
        // NOTE: Similarly to the above, this matches method names in method
        // calls. Do we want to match these, or just declarations?
        Tree methodSelect = methodInvocationTree.getMethodSelect();

        Name identifier;

        if (methodSelect instanceof MemberSelectTree) {
            identifier = ((MemberSelectTree) methodSelect).getIdentifier();
        } else if (methodSelect instanceof IdentifierTree) {
            identifier = ((IdentifierTree) methodSelect).getName();
        } else {
            throw malformedMethodInvocationTree(methodInvocationTree);
        }

        return checkName(methodInvocationTree, identifier);
    }

    @Override
    public Description matchMethod(MethodTree methodTree, VisitorState visitorState) {
        // MethodTree represents the definition of a method. We want to check the name of this
        // method to see if it is acceptable.
        Name methodName = methodTree.getName();
        return checkName(methodTree, methodName);
    }

    @Override
    public Description matchVariable(VariableTree variableTree, VisitorState visitorState) {
        // VariableTree represents variable declarations (fields, local variables, parameters)
        Name variableName = variableTree.getName();
        return checkName(variableTree, variableName);
    }

    private Description checkName(Tree tree, Name identifier) {
        String name = identifier.toString();
        
        // Check for specific bad names (existing functionality)
        if (name.equals("foo") || name.equals("bar") || name.equals("baz") || 
            name.equals("temp") || name.equals("tmp") || name.equals("data") ||
            name.equals("obj") || name.equals("thing") || name.equals("stuff")) {
            return buildDescription(tree)
                    .setMessage(String.format("'%s' is a generic/meaningless identifier name", name))
                    .build();
        }
        
        // pattern rules
        
        // too short, less than two chars
        if (name.length() < 2 && !isAcceptableShortName(name)) {
            return buildDescription(tree)
                    .setMessage(String.format("Identifier '%s' is too short and not descriptive", name))
                    .build();
        }
        
        // too long, more than 50 characters
        if (name.length() > 50) {
            return buildDescription(tree)
                    .setMessage(String.format("Identifier '%s' is too long (>50 characters)", name))
                    .build();
        }
        
        // only numbers
        if (name.matches("^\\d+$")) {
            return buildDescription(tree)
                    .setMessage(String.format("Identifier '%s' consists only of numbers", name))
                    .build();
        }
        
        // excessive underscores or repeated characters
        if (name.contains("__") || name.matches(".*([a-zA-Z])\\1{3,}.*")) {
            return buildDescription(tree)
                    .setMessage(String.format("Identifier '%s' contains excessive repeated characters", name))
                    .build();
        }

        // starts or ends with underscores 
        if ((name.startsWith("_") || name.endsWith("_"))) {
            return buildDescription(tree)
                    .setMessage(String.format("Identifier '%s' should not start or end with underscores", name))
                    .build();
        }
        
        // all uppercase (not constant)
        if (name.matches("^[A-Z_]+$") && !isConstant(tree)) {
            return buildDescription(tree)
                    .setMessage(String.format("Identifier '%s' should not be all uppercase unless it's a constant", name))
                    .build();
        }

        return Description.NO_MATCH;
    }

    private boolean isAcceptableShortName(String name) 
    {
        // single-letter names that are acceptable in some cases
        return name.matches("^[ijklmnpqr]$");  // Common loop counters
    }
    
    private boolean isConstant(Tree tree) {
        // check if the variable is a constant (static final)
        if (tree instanceof VariableTree) {
            VariableTree varTree = (VariableTree) tree;
            String modifiers = varTree.getModifiers().toString();
            return modifiers.contains("static") && modifiers.contains("final");
        }
        return false;
    }

    private static final IllegalStateException malformedMethodInvocationTree(MethodInvocationTree tree) {
        return new IllegalStateException(String.format("Method name %s is malformed.", tree));
    }
}