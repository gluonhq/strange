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
package org.redfx.strange.gate;

import org.redfx.strange.Block;
import org.redfx.strange.BlockGate;
import org.redfx.strange.Complex;
import org.redfx.strange.ControlledBlockGate;
import org.redfx.strange.Gate;
import org.redfx.strange.Program;
import org.redfx.strange.Step;
import static org.redfx.strange.gate.Add.cache;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author johan
 */
public class AddModulus extends BlockGate<AddModulus> {

    Block block;
    static HashMap<Integer, Block> cache = new HashMap<>();

    
    /**
     * Add the qubit in the x register and the y register mod N, result is in x
     * @param x0 start idx x register
     * @param x1 end idx x register
     * @param y0 start idx y register
     * @param y1 end idx y register
     * x_0 ----- y_0 + x_0 
     * x_1 ----- y+1 + x_1
     * y_0 ----- y_0
     * y_1 ----- y_1
     * ANC(0)--- ANC(0)
     * the qubit following y_1 should be 0 (and will be 0 after this gate)
     * qubit at x_1 should be 0 and qubit at y_1 should be 0 (overflow)
     */
    public AddModulus(int x0, int x1, int y0, int y1, int N) {
        super();
        setIndex(x0);
        y1 = y1-x0;
        y0 = y0-x0;
        x1 = x1 - x0;
        x0 = 0;
        assert(y0 == x1+1);
        int hash = 1000000 * x0 + 10000*x1+ 100*y0 + 10 * y1 + N;
        this.block = cache.get(hash);
        if (this.block == null) {
            this.block = createBlock(x0, x1, y0, y1, N);
            cache.put(hash, block);
        }
        setBlock(block);
    }
    
    public Block createBlock(int x0, int x1, int y0, int y1, int N) {
        int dim = y1-x0+2;
        Block answer = new Block("AddModulus", y1-x0+2);
        int n = x1-x0;
//        int dim = 2 * (n+1)+1;

        Add add = new Add(x0, x1, y0, y1);
        answer.addStep(new Step(add));

        AddInteger min = new AddInteger(x0,x1,N).inverse();
        answer.addStep(new Step(min));
        answer.addStep(new Step(new Cnot(x1,dim-1)));
        AddInteger addN = new AddInteger(x0,x1,N);
        ControlledBlockGate cbg = new ControlledBlockGate(addN, x0,dim-1);
        answer.addStep(new Step(cbg));

        Add add2 = new Add(x0,x1,y0,y1).inverse();
        answer.addStep(new Step(add2));
        answer.addStep(new Step(new X(dim-1)));

        Block block = new Block(1);
        block.addStep(new Step(new X(0)));
        ControlledBlockGate cbg2 = new ControlledBlockGate(block, dim-1, x1);
        answer.addStep(new Step(cbg2));

        Add add3 = new Add(x0,x1,y0,y1);
        answer.addStep (new Step(add3));
       
        return answer;
    }

}
