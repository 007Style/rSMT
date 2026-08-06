/*
 * rSMT.java
 *
 * Created on January 11, 2009, 8:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package rsmt;

import java.io.*;
import java.util.*;

/**
 *
 * @author dsingley
 */
public class rSMT {
    
    public Random generator = new Random();
    public int clock = 0;
    public Vector vInst = new Vector();;
    public Vector vInstBase = new Vector();;
    public Vector vInstFinished = null;
    public Vector vInstFinishedBase = null;
    public genInstructions genI  = null;
    public genInstructions genIBase  = null;
    public genInstructions genIOrig  = null;
    public int instructionNum = 10000;
    public int rDelay = 0;
    public int percentInt = 50;
    public int rSMT_availPercent = 50;
    public int rSMT_dependsPercent = 20;
    public int intInst = 0;
    public int intInstrSMT = 0;
    public int r_intInst = 0;
    public boolean execBranch = false;
    public genInstructions.rInst[] curInst_FXU = null;            
    public genInstructions.rInst[] curInst_FPU = null;
    public genInstructions.rInst[] curInst_B = null;
    public int rCycles = 0;
    public int normCycles = 0;
    
    public final static int  fpCycles = 6;
    public final static int  fxCycles = 5;
    public final static int  brCycles = 4;
    public final static int nopCycles = 0;
    
    
    /** Creates a new instance of rSMT */
    public rSMT(int inst, int delay, int perc, int rAvail, int rDepends) 
    {
        instructionNum = inst;
        rDelay = delay;
        percentInt = perc;
        rSMT_availPercent = rAvail;
        rSMT_dependsPercent = rDepends;
        
        long seed = System.currentTimeMillis();
        genI = new genInstructions(seed);
        genIBase = new genInstructions(seed);
        genIOrig = new genInstructions(seed);
        
        vInst = genI.genInst(inst, perc);
        vInstBase = genIBase.genInst(inst, perc);
        vInstFinished = new Vector(instructionNum);
        vInstFinishedBase = new Vector(instructionNum);
        for(int i=0; i<instructionNum; i++) vInstFinished.add(null);
        for(int i=0; i<instructionNum; i++) vInstFinishedBase.add(null);
        
        
        //vInstBase = copyVector(vInst);
        //vInstBase.clear();
        //vInstBase.addAll(vInst);
        //vInstBase = new Vector(vInst);
        
        rReset();
        rSimulate();
        rSummary();
        rReset();
        normSimulate();
        normSummary();
        finalSummary();
        
    }
    
    public Vector copyVector(Vector v1)
    {
        genInstructions.rInst[] array = new genInstructions.rInst[v1.size()];
        v1.toArray(array);
        
        List l = Arrays.asList(array);
        Vector v2 = new Vector(l);
        v2 = new Vector(Arrays.asList(array));
        
        return v2;
        
    }
    
    public void rReset()
    {
        //vInst = new Vector(vInstBase);
        curInst_FXU = new genInstructions.rInst[2];
        curInst_FPU = new genInstructions.rInst[2];
        curInst_B   = new genInstructions.rInst[2];
        intInst = 0;
        clock = 0;
        execBranch = false;
    }
    
    /*
    public void rSimulate()
    {
        System.out.println("(1) Simulating rSMT Activated... START");
        while(!vInst.isEmpty())
        {
            clock++;
            if((curInst_FXU[0] == null) && (!vInst.isEmpty()))
            {
                curInst_FXU[0] = (genInstructions.rInst)vInst.remove(0);
                if(curInst_FXU[0].index < 4){  r_intInst++; }
            }
            if((curInst_FXU[1] == null) && (rSMT_cycle()) && (!rSMT_depends()) && (!vInst.isEmpty()))
            {
                genInstructions.rInst tempEvalInst = (genInstructions.rInst)vInst.get(0);
                if(tempEvalInst.index < 4)
                {
                    intInstrSMT++;
                    curInst_FXU[1] = (genInstructions.rInst)vInst.remove(0);
                    System.out.println("CYCLE: " + clock + " rSMT instruction: " + genI.instName(curInst_FXU[1]));
                }
            }
            if(curInst_FXU[0] != null)
            {
                if(curInst_FXU[0].curClock == curInst_FXU[0].doneClock)
                {
                    vInstFinished.add(genI.execute(curInst_FXU[0]));
                    curInst_FXU[0] = null;
                }
                else{ curInst_FXU[0].curClock++; }
            }
            if(curInst_FXU[1] != null)
            {
                if(curInst_FXU[1].curClock == (curInst_FXU[1].doneClock + rDelay))
                {
                    vInstFinished.add(genI.execute(curInst_FXU[1]));
                    curInst_FXU[1] = null;
                }
                else{ curInst_FXU[1].curClock++; }
            }
        }
        rCycles = clock;
        System.out.println("(1) Simulating rSMT Activated... FINISHED");
    }
    */
    
    public void rSimulate()
    {
        System.out.println("(2) Simulating rSMT Activated... START");
        while(!vInst.isEmpty())
        {
            clock++;
            //Issue (single cycle issue)
            if((!vInst.isEmpty()) && (!execBranch))
            {
                if((curInst_FXU[0] == null) && ((genInstructions.rInst)vInst.get(0)).isFXU())
                {
                    curInst_FXU[0] = (genInstructions.rInst)vInst.remove(0);
                    r_intInst++;
                }
                
                else if((curInst_FXU[1] == null) && (rSMT_cycle()) && (!rSMT_depends()) && (!vInst.isEmpty()))
                {
                    genInstructions.rInst tempEvalInst = (genInstructions.rInst)vInst.get(0);
                    if(tempEvalInst.index < 4)
                    {
                        intInstrSMT++;
                        curInst_FXU[1] = (genInstructions.rInst)vInst.remove(0);
                        r_intInst++;
                        System.out.println("CYCLE: " + clock + " rSMT instruction: " + genI.instName(curInst_FXU[1]));
                    }
                }
                
                else if((curInst_FPU[0] == null) && ((genInstructions.rInst)vInst.get(0)).isFPU())
                {
                    curInst_FPU[0] = (genInstructions.rInst)vInst.remove(0);
                }
                else if((curInst_B[0] == null) && ((genInstructions.rInst)vInst.get(0)).isB())
                {
                    curInst_B[0] = (genInstructions.rInst)vInst.remove(0);
                    execBranch = true;
                }
                else if(((genInstructions.rInst)vInst.get(0)).isNOP())
                {
                    vInstFinishedBase.set(((genInstructions.rInst)vInst.get(0)).order, genI.execute(((genInstructions.rInst)vInst.get(0))));
                    vInst.remove(0);
                }
            }
            //Execute / Retire
            if(curInst_FXU[0] != null)
            {
                if(curInst_FXU[0].curClock == curInst_FXU[0].doneClock)
                {
                    //System.out.println("exe:   " +genI.execute(curInst_FXU[0]));
                    vInstFinishedBase.set(curInst_FXU[0].order, genI.execute(curInst_FXU[0]));
                    curInst_FXU[0] = null;
                }
                else{ curInst_FXU[0].curClock++; }
            }
            
            if(curInst_FXU[1] != null)
            {
                if(curInst_FXU[1].curClock == (curInst_FXU[1].doneClock + rDelay))
                {
                    vInstFinished.add(genI.execute(curInst_FXU[1]));
                    curInst_FXU[1] = null;
                }
                else{ curInst_FXU[1].curClock++; }
            }
            
            if(curInst_FPU[0] != null)
            {
                if(curInst_FPU[0].curClock == curInst_FPU[0].doneClock)
                {
                    vInstFinishedBase.set(curInst_FPU[0].order, genI.execute(curInst_FPU[0]));
                    curInst_FPU[0] = null;
                }
                else{ curInst_FPU[0].curClock++; }
            }
            if(curInst_B[0] != null)
            {
                if(curInst_B[0].curClock == curInst_B[0].doneClock)
                {
                    vInstFinishedBase.set(curInst_B[0].order, genI.execute(curInst_B[0]));
                    curInst_B[0] = null;
                    execBranch = false;
                }
                else{ curInst_B[0].curClock++; }    
            }
        }
        rCycles = clock;
        System.out.println("(2) Simulating rSMT Activated... FINISHED");
    }
    
    public void normSimulate()
    {
        System.out.println("(3) Simulating rSMT Deactivated... START");
        while(!vInstBase.isEmpty())
        {
            clock++;
            //Issue (single cycle issue)
            if((!vInstBase.isEmpty()) && (!execBranch))
            {
                if((curInst_FXU[0] == null) && ((genInstructions.rInst)vInstBase.get(0)).isFXU())
                {
                    curInst_FXU[0] = (genInstructions.rInst)vInstBase.remove(0);
                    if(curInst_FXU[0].index < 4){  intInst++; }
                }
                else if((curInst_FPU[0] == null) && ((genInstructions.rInst)vInstBase.get(0)).isFPU())
                {
                    curInst_FPU[0] = (genInstructions.rInst)vInstBase.remove(0);
                }
                else if((curInst_B[0] == null) && ((genInstructions.rInst)vInstBase.get(0)).isB())
                {
                    curInst_B[0] = (genInstructions.rInst)vInstBase.remove(0);
                    execBranch = true;
                }
                else if(((genInstructions.rInst)vInstBase.get(0)).isNOP())
                {
                    vInstFinishedBase.set(((genInstructions.rInst)vInstBase.get(0)).order, genI.execute(((genInstructions.rInst)vInstBase.get(0))));
                    vInstBase.remove(0);
                }
            }
            //Execute / Retire
            if(curInst_FXU[0] != null)
            {
                if(curInst_FXU[0].curClock == curInst_FXU[0].doneClock)
                {
                    //System.out.println("exe:   " +genI.execute(curInst_FXU[0]));
                    vInstFinishedBase.set(curInst_FXU[0].order, genI.execute(curInst_FXU[0]));
                    curInst_FXU[0] = null;
                }
                else{ curInst_FXU[0].curClock++; }
            }
            if(curInst_FPU[0] != null)
            {
                if(curInst_FPU[0].curClock == curInst_FPU[0].doneClock)
                {
                    vInstFinishedBase.set(curInst_FPU[0].order, genI.execute(curInst_FPU[0]));
                    curInst_FPU[0] = null;
                }
                else{ curInst_FPU[0].curClock++; }
            }
            if(curInst_B[0] != null)
            {
                if(curInst_B[0].curClock == curInst_B[0].doneClock)
                {
                    vInstFinishedBase.set(curInst_B[0].order, genI.execute(curInst_B[0]));
                    curInst_B[0] = null;
                    execBranch = false;
                }
                else{ curInst_B[0].curClock++; }    
            }
        }
        normCycles = clock;
        System.out.println("(3) Simulating rSMT Deactivated... FINISHED");
    }
    
    public boolean rSMT_depends()
    {
        int rGen = generator.nextInt(101);
        if(rGen <= rSMT_dependsPercent){ return false; }
        else{ return true; }
    }
    
    public boolean rSMT_cycle()
    {
        int rGen = generator.nextInt(101);
        if(rGen <= rSMT_availPercent){ return true; }
        else{ return false; }
    }
    
    public void rSummary()
    {
        System.out.println("**********************************************************");
        System.out.println("Total Cycles Ran         : " + rCycles);
        System.out.println("Total        Instructions: " + instructionNum);
        System.out.println("Integer      Instructions: " + r_intInst);
        System.out.println("Integer rSMT Instructions: " + intInstrSMT);
        System.out.println("**********************************************************");
    }
    
    public void normSummary()
    {
        System.out.println("**********************************************************");
        System.out.println("Total Cycles Ran         : " + normCycles);
        System.out.println("Total        Instructions: " + instructionNum);
        System.out.println("Integer      Instructions: " + intInst);
        System.out.println("**********************************************************");
    }
    
    public void finalSummary()
    {
        float rSMT_result = ((float)normCycles / (float)rCycles) * 100;
        System.out.println("(4)**********************************************************");
        System.out.println("rSMT performance gain    : " + rSMT_result + "%");
        System.out.println("(4)**********************************************************");
    }
    
}

