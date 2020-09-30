/*-
 * #%L
 * Strange
 * %%
 * Copyright (C) 2020 Johan Vos
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Johan Vos nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.redfx.strange.test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.redfx.strange.Block;
import org.redfx.strange.BlockGate;
import org.redfx.strange.ControlledBlockGate;
import org.redfx.strange.Program;
import org.redfx.strange.Qubit;
import org.redfx.strange.Result;
import org.redfx.strange.Step;
import org.redfx.strange.gate.Add;
import org.redfx.strange.gate.AddInteger;
import org.redfx.strange.gate.AddModulus;
import org.redfx.strange.gate.Cnot;
import org.redfx.strange.gate.Mul;
import org.redfx.strange.gate.MulModulus;
import org.redfx.strange.gate.Swap;
import org.redfx.strange.gate.X;
import org.redfx.strange.local.Computations;

/**
 *
 * @author johan
 */
public class SingleTest extends BaseGateTests {
  
      @Test
    public void addmodgate() {
        int n = 2;
        int N = 3;
        int dim = 2 * (n+1)+1;
        Program p = new Program(dim);
        Step prep = new Step();
        prep.addGates(new X(1), new X(4));
        p.addStep(prep);
        
        Step mod = new Step(new AddModulus(0,2,3,4, N));
        p.addStep(mod);
        Result result = runProgram(p);
        Qubit[] q = result.getQubits();
        System.err.println("Results: ");
        for (int i = 0; i < 7; i++) {
            System.err.println("m["+i+"]: "+q[i].measure());
        }
        assertEquals(7, q.length);
        assertEquals(1, q[0].measure());
        assertEquals(0, q[1].measure());
        assertEquals(0, q[2].measure());
        assertEquals(0, q[3].measure());
        assertEquals(1, q[4].measure());
        assertEquals(0, q[5].measure());
        assertEquals(0, q[6].measure());
    }
    
//    @Test
    public void cf () {
        double d = 340./1024;
        System.err.println("Start with d = "+d);
        int r = Computations.fraction(d, 5);
        assertEquals(3, r);
        r = Computations.fraction(.75000001, 15);
        assertEquals(4, r);
    }
      
   // @Test
    public void multiplyModGate5x3mod6p() { // 5 x 3 mod 6 = 3
        Program p = new Program(10);
        int mul = 5;
        int N = 6;
        Step prep = new Step();
        prep.addGates(new X(5), new X(6)); // 3 in high register
        Step s = new Step(new MulModulus(1,4,mul, N));
        p.addStep(prep);
        p.addStep(s);
        Result result = runProgram(p);
        Qubit[] q = result.getQubits();
        System.err.println("results: ");
        for (int i = 0; i < 9; i++) {
            System.err.println("m["+i+"]: "+q[i].measure());
        }
        assertEquals(10, q.length);
        assertEquals(0, q[0].measure());
        assertEquals(0, q[1].measure()); // q2,q1,q0,q3 should be clean
        assertEquals(0, q[2].measure());  
        assertEquals(0, q[3].measure());
        assertEquals(0, q[4].measure());
        assertEquals(1, q[5].measure()); // result in q4,q5,q6,q7
        assertEquals(1, q[6].measure());
        assertEquals(0, q[7].measure());  
        assertEquals(0, q[8].measure());  
        assertEquals(0, q[9].measure());  
    }
    
  //  @Test // 
    public void expmul3p4mod7() { // 3^4 = 81 -> mod 7 = 4
        int length = 3; 
        // q0 -> q2: 4
        // q3 -> q6: ancilla (0 before, 0 after)
        // q7 -> q10: result, start with 1
        int a = 3;
        int mod = 7;
        Program p = new Program(3 * length+3);
        Step prep = new Step(new X(2)); // exp = 4
        Step prepAnc = new Step(new X(7)); 
        p.addStep(prep);
        p.addStep(prepAnc);
        for (int i = length - 1; i > -1; i--) {
            int m = 1;
            for (int j = 0; j < 1 << i; j++) {
                m = m*a %mod;
            }
//            int m = (int) Math.pow(a, 1 << i);
            System.err.println("M = "+m);
            MulModulus mul = new MulModulus(length, 2 * length, m, mod);
            ControlledBlockGate cbg = new ControlledBlockGate(mul, length, i);
            p.addStep(new Step(cbg));
        }
          //          MulModulus mul = new MulModulus(3, 6, 2, 7);
//p.addStep(new Step(mul));
        Result result = runProgram(p);
        Qubit[] q = result.getQubits();
        assertEquals(12, q.length);
        System.err.println("results: ");
        for (int i = 0; i < 12; i++) {
            System.err.println("m["+i+"]: "+q[i].measure());
        }
        assertEquals(0, q[0].measure());
        assertEquals(0, q[1].measure());
        assertEquals(1, q[2].measure());
        assertEquals(0, q[3].measure());
        assertEquals(0, q[4].measure());
        assertEquals(0, q[5].measure());
        assertEquals(0, q[6].measure());
        assertEquals(0, q[7].measure());
        assertEquals(0, q[8].measure());
        assertEquals(1, q[9].measure());
        assertEquals(0, q[10].measure());
        assertEquals(0, q[11].measure());
    }
    
    
//    @Test // 
    public void expmul() {
        int length = 2;
        int a = 3;
        Program p = new Program(3 * length);
        Step prepAnc = new Step(new X(3 * length -1));
        p.addStep(prepAnc);
        for (int i = 0; i < length; i++) {
            int m = a << (length -1 - i); // 2^(n-1-i)
            Mul mul = new Mul(length, 2 * length-1, a);
            if (i < length -1) {
                Swap swap = new Swap(i, length-1);
                p.addStep(new Step(swap));
            }
            ControlledBlockGate cbg = new ControlledBlockGate(mul, length, length -1);
            p.addStep(new Step(cbg));
            if (i < length -1) {
                Swap swap = new Swap(i, length-1);
                p.addStep(new Step(swap));
            }
        }
        Result result = runProgram(p);
        Qubit[] q = result.getQubits();
        assertEquals(6, q.length);
        System.err.println("results: ");
        for (int i = 0; i < 6; i++) {
            System.err.println("m["+i+"]: "+q[i].measure());
        }
        assertEquals(0, q[0].measure());
        assertEquals(0, q[1].measure());
        assertEquals(0, q[2].measure());
        assertEquals(0, q[3].measure());
        assertEquals(0, q[4].measure());
        assertEquals(1, q[5].measure());
    }
    //@Test
    public void compareBlockok() {
     //   for (int a = 0; a < 4; a++) {
           // for (int b = 0; b < 4; b++) {
                Step prep = new Step();
                int a = 1; int b = 1;
                prep.addGate(new X(1));
                prep.addGate(new X(2));
                Program p = new Program(3);
              //  Step s0 = new Step(new Toffoli(1, 2, 3));
                Step s0 = new Step(new Cnot(1,2));
                p.addSteps(prep, s0);//, s1, s2);
                Result normal = runProgram(p);
                Qubit[] normalQ = normal.getQubits();
                System.err.println("CREATE BLOCK NOW");
                Block carry = new Block("carry", 3);
//                carry.addStep(new Step(new Toffoli(1, 2, 3)));
                carry.addStep(new Step(new Cnot(1,2)));
                Program bp = new Program(3);
                Step bs0 = new Step(new BlockGate(carry, 0));
                bp.addSteps(prep, bs0);
                Result blockResult = runProgram(bp);
                Qubit[] blockQ = blockResult.getQubits();
                System.err.println("a = "+a+" and b = "+b);
                assertEquals(normalQ.length, blockQ.length);
                for (int i = 0; i < normalQ.length; i++) {
                    System.err.println("i = "+i);
                    assertEquals(normalQ[i].measure(), blockQ[i].measure());
                }
         //   }
    //    }
    }

            
  //  @Test // 
    public void expmul3p3() { // 3^3 = 27 -> mod 8 = 3
        int length = 3;
        int N = 8;
        // q0 -> q2: x (3)
        // q3 -> q5: ancilla (0 before, 0 after)
        // q6 -> q8: result
        int a = 3;
        Program p = new Program(3 * length);
        Step prep = new Step(new X(0), new X(1));
        Step prepAnc = new Step(new X(2 * length));
        p.addStep(prep);
        p.addStep(prepAnc);
        for (int i = length - 1; i > -1; i--) {
//            int m = (int) Math.pow(a, 1 << i);
            int m = 1;
            for (int j = 0; j < 1 << i; j++) {
                m = m*a %N;
            }
            System.err.println("M = " + m);
            Mul mul = new Mul(length, 2 * length - 1, m);
            ControlledBlockGate cbg = new ControlledBlockGate(mul, length, i);
            p.addStep(new Step(cbg));
        }
        Result result = runProgram(p);
        Qubit[] q = result.getQubits();
        assertEquals(9, q.length);
        System.err.println("results: ");
        for (int j = 0; j < 9; j++) {
            System.err.println("m[" + j + "]: " + q[j].measure());
        }
        assertEquals(1, q[0].measure());
        assertEquals(1, q[1].measure());
        assertEquals(0, q[2].measure());
        assertEquals(0, q[3].measure());
        assertEquals(0, q[4].measure());
        assertEquals(0, q[5].measure());
        assertEquals(1, q[6].measure());
        assertEquals(1, q[7].measure());
        assertEquals(0, q[8].measure());
    }
    
  //  @Test
    public void nonomultiplyModGate5x3mod6() { // 5 x 3 mod 6 = 3
        Program p = new Program(9);
        int mul = 5;
        int N = 6;
        Step prep = new Step();
        prep.addGates(new X(4), new X(5)); // 3 in high register
        Step s = new Step(new MulModulus(0,3,mul, N));
        p.addStep(prep);
        p.addStep(s);
        Result result = runProgram(p);
        Qubit[] q = result.getQubits();
        System.err.println("results: ");
        for (int i = 0; i < 9; i++) {
            System.err.println("m["+i+"]: "+q[i].measure());
        }
        assertEquals(9, q.length);
        assertEquals(0, q[0].measure()); // q2,q1,q0,q3 should be clean
        assertEquals(0, q[1].measure());  
        assertEquals(0, q[2].measure());
        assertEquals(0, q[3].measure());
        assertEquals(1, q[4].measure()); // result in q4,q5,q6,q7
        assertEquals(1, q[5].measure());
        assertEquals(0, q[6].measure());  
        assertEquals(0, q[7].measure());  
        assertEquals(0, q[8].measure());  
    }
    
 // @Test
  public void expmul3p4mod7s0() { // 3^4 = 81 -> mod 7 = 4
        int length = 3; 
        // q0 -> q3: x (4)
        // q4 -> q7: ancilla (0 before, 0 after)
        // q8 -> q11: result
        // q12: ancilla
        int a = 3;
        int mod = 7;
        Program p = new Program(4 * length+1);
        Step prep = new Step(new X(0), new X(1));
        Step prepAnc = new Step(new X(2 * length));
        p.addStep(prep);
        p.addStep(prepAnc);

        for (int i = 0; i < length; i++) {
            int m = (int) Math.pow(a, 1 <<  i);
            System.err.println("M = "+m);
            MulModulus mul = new MulModulus(length, 2 * length-1, m, mod);
            ControlledBlockGate cbg = new ControlledBlockGate(mul, length, i);
            p.addStep(new Step(cbg));
        }
        Result result = runProgram(p);
        Qubit[] q = result.getQubits();
        assertEquals(9, q.length);
        System.err.println("results: ");
        for (int i = 0; i < 9; i++) {
            System.err.println("m["+i+"]: "+q[i].measure());
        }
        assertEquals(1, q[0].measure());
        assertEquals(1, q[1].measure());
        assertEquals(0, q[2].measure());
        assertEquals(0, q[3].measure());
        assertEquals(0, q[4].measure());
        assertEquals(0, q[5].measure());
        assertEquals(0, q[6].measure());
        assertEquals(1, q[7].measure());
        assertEquals(0, q[8].measure());
    }
  
    //  @Test
    public void add61() {
        Program p = new Program(6);
        Step prep = new Step();
        prep.addGates(new X(1), new X(2), new X(3));
        p.addStep(prep);
        Add add = new Add(0,2,3,5);
        p.addStep(new Step(add));
        Result result = runProgram(p);
        Qubit[] q = result.getQubits();
        assertEquals(6, q.length);
        assertEquals(1, q[0].measure());
        assertEquals(1, q[1].measure());  
        assertEquals(1, q[2].measure());
        assertEquals(1, q[3].measure()); 
        assertEquals(0, q[4].measure());
        assertEquals(0, q[5].measure());  
    }
    
  //  @Test
    public void add0num0() {
        Program p = new Program(1);
        Step prep = new Step();
        p.addStep(prep);
        AddInteger add = new AddInteger(0, 0, 0);
        p.addStep(new Step(add));
        System.err.println("RUN ADD0NUM0");
        try {
            Result result = runProgram(p);

            System.err.println("RESULT FROM RUN = " + result);
            Qubit[] q = result.getQubits();
            System.err.println("GOT QUBITS: " + q);
            System.err.println("GOT QUBITSlength: " + q.length);
            assertEquals(1, q.length);
            assertEquals(0, q[0].measure());
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

  //    @Test
    public void add21() {
        Program p = new Program(4);
        Step prep = new Step();
        prep.addGates(new X(1), new X(2));
        p.addStep(prep);
        Add add = new Add(0,1,2,3);
        p.addStep(new Step(add));
        Result result = runProgram(p);
        Qubit[] q = result.getQubits();
        assertEquals(4, q.length);
        assertEquals(1, q[0].measure());
        assertEquals(1, q[1].measure());  
        assertEquals(1, q[2].measure());
        assertEquals(0, q[3].measure()); 
    }
    
  public static void main(String[] args) {
      SingleTest st = new SingleTest();
      st.expmul3p4mod7();
  }
}
