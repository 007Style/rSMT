/*
 * genInstructions.java
 *
 * Created on January 11, 2009, 8:08 PM
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
public class genInstructions {
    
    public Random generator = null;
    public String[] instList = new String[] {"add","sub","mul","div","fadd","fsub","fmul","fdiv","b","nop"};
    
    /** Creates a new instance of genInstructions */
    public genInstructions() 
    {
        generator = new Random();
    }
    
    public genInstructions(long seed) 
    {
        generator = new Random(seed);
    }
    
    public Vector genInst(int number, int percent)
    {
        System.out.println("(1) Generating Instructions... Number of Instructions:" + number + " %FXU: " + percent);
        Vector result = new Vector();
        for(int i=0; i<number; i++)
        {
            int index = 9;
            rInst rI = new rInst();
            if(percent == 0){ index = generator.nextInt(instList.length); }
            else
            {
                int base = generator.nextInt(101);
                if(base >= percent)
                {
                    index = generator.nextInt(6) + 4;
                }
                else
                {
                    index = generator.nextInt(4);
                }
            }
            rI.inst = instList[index];  rI.index = index;
            if(index <= 3)
            {
                rI.op1 = generator.nextInt();
                rI.op2 = generator.nextInt();
                rI.doneClock = rSMT.fxCycles;
            }
            if((index > 3) && (index <= 7))
            {
                rI.dop1 = generator.nextDouble();
                rI.dop2 = generator.nextDouble();
                rI.doneClock = rSMT.fpCycles;
            }
            if(index == 8)
            { 
                rI.branch = true; 
                rI.doneClock = rSMT.brCycles;
            }
            rI.order = i;
            result.add(rI);
        }
        System.out.println("Number of successfully Generated instructions: " + result.size());
        return result;
    }
    
    public rInst execute(rInst exe)
    {
        if(exe.branch){ exe.txtResult = "branch"; }
        if(exe.index == 9){ exe.txtResult = "noop"; }
        if(exe.index <= 3)
        {
            exe.txtResult = exe.inst + "(" + exe.op1 + "," + exe.op2 +")";
            if(exe.index == 0){ exe.result = exe.op1 + exe.op2; }
            if(exe.index == 1){ exe.result = exe.op1 - exe.op2; }
            if(exe.index == 2){ exe.result = exe.op1 * exe.op2; }
            if(exe.index == 3){ exe.result = exe.op1 / exe.op2; }
        }
        if((exe.index > 3) && (exe.index <= 7))
        {
            exe.txtResult = exe.inst + "(" + exe.dop1 + "," + exe.dop2 +")";
            if(exe.index == 0){ exe.dresult = exe.dop1 + exe.dop2; }
            if(exe.index == 1){ exe.dresult = exe.dop1 - exe.dop2; }
            if(exe.index == 2){ exe.dresult = exe.dop1 * exe.dop2; }
            if(exe.index == 3){ exe.dresult = exe.dop1 / exe.dop2; }
           
        }
        return exe;
    }
    
    public String instName(rInst exe)
    {
        if(exe.branch){ exe.txtResult = "branch"; }
        if(exe.index == 9){ exe.txtResult = "noop"; }
        if(exe.index <= 3)
        {
            exe.txtResult = exe.inst + "(" + exe.op1 + "," + exe.op2 +")";
        }
        if((exe.index > 3) && (exe.index <= 7))
        {
            exe.txtResult = exe.inst + "(" + exe.dop1 + "," + exe.dop2 +")";
        }
        //System.out.println(exe.txtResult);
        return exe.txtResult;
    }
    
    public class rInst
    {
        int order = 0;
        int index = 9;
        String inst = "nop";
        int op1 = 0;
        int op2 = 0;
        int result = 0;
        double dop1 = 0;
        double dop2 = 0;
        double dresult = 0;
        boolean branch = false;
        String txtResult = "";
        int curClock = 0;
        int doneClock = 0;
        
        public boolean isFXU()
        {
            if((index == 0) || (index == 1) || (index == 2) || (index == 3))
            {
                return true;
            }
            else{ return false; }
        }
        
        public boolean isFPU()
        {
            if((index == 4) || (index == 5) || (index == 6) || (index == 7))
            {
                return true;
            }
            else{ return false; }
        }
        
        public boolean isB()
        {
            if((index == 8))
            {
                return true;
            }
            else{ return false; }
        }
        
        public boolean isNOP()
        {
            if((index == 9))
            {
                return true;
            }
            else{ return false; }
        }
    }
    
}
