package edu.appstate.cs.checker;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;

import java.util.*;

import javax.lang.model.element.Name;

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
}
