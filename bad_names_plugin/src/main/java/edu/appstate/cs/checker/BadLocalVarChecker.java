package edu.appstate.cs.checker;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;

@AutoService(BugChecker.class)
@BugPattern(
        name = "BadLocalVarChecker",
        summary = "Detects unnecessary local variables that are immediately returned or only used once",
        severity = WARNING,
        linkType = CUSTOM,
        link = "https://github.com/nicholasshelnutt/error-prone-lab"
)
public class BadLocalVarChecker extends BugChecker implements 
    BugChecker.MethodTreeMatcher
{
    // holds var info
    private static class VarInfo
    {
        private final VariableTree dtree;
        private int usageCount = 0;
        private boolean reassigned = false;
        private boolean immediatlyReturned = false;
        private boolean usedInLoop = false;

        public VarInfo(VariableTree dtree) 
        {
            this.dtree = dtree;
        }

        public VariableTree getDtree() 
        {
            return dtree;
        }

        public int getUsageCount() 
        {
            return usageCount;
        }

        public void incrementUsageCount() 
        {
            usageCount++;
        }

        public boolean isReassigned() 
        {
            return reassigned;
        }

        public void setReassign(boolean reassigned)
        {
            this.reassigned = reassigned;
        }

        public boolean isImmediatlyReturned() 
        {
            return immediatlyReturned;
        }

        public void setImmediatelyReturned(boolean immediatlyReturned) 
        {
            this.immediatlyReturned = immediatlyReturned;
        }

        public boolean isUsedInLoop() 
        {
            return usedInLoop;
        }

        public void setUsedInLoop(boolean usedInLoop) 
        {
            this.usedInLoop = usedInLoop;
        }
    }

    // checks if var name looks like a loop counter
    private boolean isLoopCounter(String varName)
    {
        return varName.length() == 1 && "ijklmnpqr".contains(varName);
    }

    private static class MethodAnalyzer extends TreeScanner<Void, Void>
    {
        private boolean inVarDecl = false;
        private boolean inLoopBody = false;
        private boolean inReturnStmt = false;

        // current var being checked
        private String targetVarName = null;
        private VariableTree targetVarDecl = null;
        private int usageCount = 0;
        private boolean isReassigned = false;
        private boolean isUsedInLoop = false;
        private boolean isImmediateReturn = false;

        public VarInfo analyzeVariable(String varName, BlockTree methodBody)
        {
            resetAnalysisState();
            this.targetVarName = varName;

            // first; find declaration
            findVariableDeclaration(methodBody);

            if (targetVarDecl == null) 
            {
                return null; // variable not found
            }

            // second: usage patterns
            analyzeUsagePatterns(methodBody);

            // third: immediate return pattern
            checkImmediateReturn(methodBody);

            // create & return var info
            VarInfo info = new VarInfo(targetVarDecl);
            setVariableInfoFields(info);
            return info;
        }

        private void resetAnalysisState()
        {
            targetVarName = null;
            targetVarDecl = null;
            usageCount = 0;
            isReassigned = false;
            isUsedInLoop = false;
            isImmediateReturn = false;
            inVarDecl = false;
            inLoopBody = false;
            inReturnStmt = false;
        }

        private void findVariableDeclaration(BlockTree methodBody)
        {
            for (StatementTree stmt : methodBody.getStatements()) 
            {
                if (stmt instanceof VariableTree) 
                {
                    VariableTree varTree = (VariableTree) stmt;
                    if (varTree.getName().toString().equals(targetVarName))
                    {
                        targetVarDecl = varTree;
                        break; // found decl
                    }
                }
                    
            }
        }

        private void analyzeUsagePatterns(BlockTree methodBody)
        {
            // reset traversal
            inVarDecl = false;
            inLoopBody = false;
            inReturnStmt = false;
            usageCount = 0;
            isReassigned = false;
            isUsedInLoop = false;
            
            scan(methodBody, null);
        }

        private void checkImmediateReturn(BlockTree methodBody)
        {
            // check for immediate return
            StatementTree prevStmt = null;
            for (StatementTree stmt : methodBody.getStatements())
            {
                if (prevStmt instanceof VariableTree && stmt instanceof ReturnTree)
                {
                    VariableTree varTree = (VariableTree) prevStmt;
                    ReturnTree returnTree = (ReturnTree) stmt;

                    if (varTree.getName().toString().equals(targetVarName) && returnTree.getExpression() instanceof IdentifierTree)
                    {
                        IdentifierTree returnedVar = (IdentifierTree) returnTree.getExpression();
                        if (returnedVar.getName().toString().equals(targetVarName))
                        {
                            isImmediateReturn = true;
                            break; // found immediate return
                        }
                    }
                }
                prevStmt = stmt;
            }
        }

        private void setVariableInfoFields(VarInfo info)
        {
            if (isImmediateReturn) 
            {
                info.setImmediatelyReturned(true);
            }

            if (isUsedInLoop) 
            {
                info.setUsedInLoop(true);
            }

            if (isReassigned) 
            {
                info.setReassign(true);
            }

            for (int i = 0; i < usageCount; i++)
            {
                info.incrementUsageCount();
            }
        }

        // override methods
        @Override 
        public Void visitForLoop(ForLoopTree tree, Void unused)
        {
            boolean wasInLoop = inLoopBody;
            inLoopBody = true;
            Void result = super.visitForLoop(tree, unused);
            inLoopBody = wasInLoop; // restore state
            return result;
        }

        @Override
        public Void visitEnhancedForLoop(EnhancedForLoopTree tree, Void unused)
        {
            boolean wasInLoop = inLoopBody;
            inLoopBody = true;
            Void result = super.visitEnhancedForLoop(tree, unused);
            inLoopBody = wasInLoop;
            return result;
        }

        @Override
        public Void visitWhileLoop(WhileLoopTree tree, Void unused)
        {
            boolean wasInLoop = inLoopBody;
            inLoopBody = true;
            Void result = super.visitWhileLoop(tree, unused);
            inLoopBody = wasInLoop; // restore state
            return result;
        }

        @Override
        public Void visitDoWhileLoop(DoWhileLoopTree tree, Void unused)
        {
            boolean wasInLoop = inLoopBody;
            inLoopBody = true;
            Void result = super.visitDoWhileLoop(tree, unused);
            inLoopBody = wasInLoop; // restore state
            return result;
        }

        @Override
        public Void visitReturn(ReturnTree tree, Void unused)
        {
            boolean wasInReturn = inReturnStmt;
            inReturnStmt = true;
            Void result = super.visitReturn(tree, unused);
            inReturnStmt = wasInReturn;
            return result;
        }

        @Override
        public Void visitVariable(VariableTree tree, Void unused)
        {
            inVarDecl = true;
            Void result = super.visitVariable(tree, unused);
            inVarDecl = false;
            return result;
        }

        @Override
        public Void visitIdentifier(IdentifierTree tree, Void unused)
        {
            if (!inVarDecl && tree.getName().toString().equals(targetVarName)) 
            {
                usageCount++;
                if (inLoopBody) 
                {
                    isUsedInLoop = true;
                }
            }
            return super.visitIdentifier(tree, unused);
        }

        @Override
        public Void visitAssignment(AssignmentTree tree, Void unused)
        {
            // check if reassignment
            ExpressionTree var = tree.getVariable();
            if (var instanceof IdentifierTree)
            {
                String varName = ((IdentifierTree) var).getName().toString();
                if (varName.equals(targetVarName))
                {
                    isReassigned = true;
                }
            }
            return super.visitAssignment(tree, unused);
        }
    }
}
