/*
 * Main.java
 *
 * Created on January 11, 2009, 8:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package rsmt;

/**
 *
 * @author dsingley
 */
public class Main {
    
    /** Creates a new instance of Main */
    public Main() {
        rSMT r = new rSMT(10000, 0, 50, 50, 20);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("java -jar rSMT <num instructions to gen> <rSMT delay to simulate> <% int inst 0-100> <% rSMT unit avail 0-100> <% inst depends on next inst in code>");
        System.out.println("NOTE: ****  Simulator does not simulate rSMT reject ops || piplining disabled || single issue/cycle  ****");
        System.out.println("");
        if(args.length == 0)
        {
            rSMT r = new rSMT(100, 0, 50, 50, 20);
        }
        else
        {
            try
            {
                rSMT r = new rSMT(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
            }
            catch(Exception e)
            {
                System.out.println("INVALID ARGS");
            }
        }
    }
    
}
