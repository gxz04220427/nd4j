/*
 *
 *  * Copyright 2015 Skymind,Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 *
 */

package org.nd4j.linalg.api.buffer;

import org.bytedeco.javacpp.DoublePointer;

import org.bytedeco.javacpp.indexer.DoubleIndexer;
import org.bytedeco.javacpp.indexer.Indexer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.nd4j.linalg.BaseNd4jTest;
import org.nd4j.linalg.api.buffer.util.DataTypeUtil;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.factory.Nd4jBackend;
import org.nd4j.linalg.util.SerializationUtils;


import java.io.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Double data buffer tests
 *
 * This tests the double buffer data type
 * Put all buffer related tests here
 *
 * @author Adam Gibson
 */
@RunWith(Parameterized.class)
public  class DoubleDataBufferTest extends BaseNd4jTest {
    DataBuffer.Type initialType;

    public DoubleDataBufferTest(Nd4jBackend backend) {
        super(backend);
        initialType = Nd4j.dataType();
    }



    @Before
    public void before() {

        DataTypeUtil.setDTypeForContext(DataBuffer.Type.DOUBLE);
    }

    @After
    public void after() {
        DataTypeUtil.setDTypeForContext(initialType);
    }

    @Test
    public void testPointerCreation() {
        DoublePointer floatPointer = new DoublePointer(1,2,3,4);
        Indexer indexer = DoubleIndexer.create(floatPointer);
        DataBuffer buffer = Nd4j.createBuffer(floatPointer, DataBuffer.Type.DOUBLE,4,indexer);
        DataBuffer other = Nd4j.createBuffer(new double[]{1,2,3,4});
        assertArrayEquals(other.asDouble(),buffer.asDouble(), 0.001);
    }

    @Test
    public void testGetSet() throws Exception {
        double[] d1 = new double[]{1, 2, 3, 4};
        DataBuffer d = Nd4j.createBuffer(d1);
        double[] d2 = d.asDouble();
        assertArrayEquals(d1, d2, 1e-1f);

    }



    @Test
    public void testSerialization2() throws Exception {
        INDArray[] arr = new INDArray[]{
                Nd4j.ones(1,10),
          //      Nd4j.ones(5,10).getRow(2)
        };

        for(INDArray a : arr) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try(ObjectOutputStream oos = new ObjectOutputStream(baos)){
                oos.writeObject(a);
                oos.flush();
            }



            byte[] bytes = baos.toByteArray();

            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);

            INDArray aDeserialized = (INDArray) ois.readObject();

            System.out.println(aDeserialized);
            assertEquals(Nd4j.ones(1,10),aDeserialized);
        }
    }


    @Test
    public void testSerialization() {
        DataBuffer buf = Nd4j.createBuffer(5);
        String fileName = "buf.ser";
        File file = new File(fileName);
        file.deleteOnExit();
        SerializationUtils.saveObject(buf, file);
        DataBuffer buf2 = SerializationUtils.readObject(file);
        //assertEquals(buf, buf2);
        assertArrayEquals(buf.asDouble(), buf2.asDouble(), 0.001);

        Nd4j.alloc = DataBuffer.AllocationMode.DIRECT;
        buf = Nd4j.createBuffer(5);
        file.deleteOnExit();
        SerializationUtils.saveObject(buf, file);
        buf2 = SerializationUtils.readObject(file);
//        assertEquals(buf, buf2);
        assertArrayEquals(buf.asDouble(), buf2.asDouble(), 0.001);
    }


    @Test
    public void testDup() throws Exception {
        double[] d1 = new double[]{1, 2, 3, 4};
        DataBuffer d = Nd4j.createBuffer(d1);
        DataBuffer d2 = d.dup();
        assertArrayEquals(d.asDouble(), d2.asDouble(), 0.0001f);
    }




    @Test
    public void testPut() throws Exception {
        double[] d1 = new double[]{1, 2, 3, 4};
        DataBuffer d = Nd4j.createBuffer(d1);
        d.put(0, 0.0);
        double[] result = new double[]{0, 2, 3, 4};
        d1 = d.asDouble();
        assertArrayEquals(d1, result, 1e-1f);
    }


    @Test
    public void testGetRange() throws Exception {
        DataBuffer buffer = Nd4j.linspace(1, 5, 5).data();
        double[] get = buffer.getDoublesAt(0, 3);
        double[] data = new double[]{1, 2, 3};
        assertArrayEquals(get, data, 1e-1f);


        double[] get2 = buffer.asDouble();
        double[] allData = buffer.getDoublesAt(0, (int)buffer.length());
        assertArrayEquals(get2, allData, 1e-1f);


    }


    @Test
    public void testGetOffsetRange() throws Exception {
        DataBuffer buffer = Nd4j.linspace(1, 5, 5).data();
        double[] get = buffer.getDoublesAt(1, 3);
        double[] data = new double[]{2, 3, 4};
        assertArrayEquals(get, data, 1e-1f);


        double[] allButLast = new double[]{2, 3, 4, 5};

        double[] allData = buffer.getDoublesAt(1, (int)buffer.length());
        assertArrayEquals(allButLast, allData, 1e-1f);

    }

    @Test
    public void testAssign() {
        DataBuffer assertion = Nd4j.createBuffer(new double[]{1, 2, 3});
        DataBuffer one = Nd4j.createBuffer(new double[]{1});
        DataBuffer twoThree = Nd4j.createBuffer(new double[]{2,3});
        DataBuffer blank = Nd4j.createBuffer(new double[]{0, 0, 0});
        blank.assign(one,twoThree);
        assertArrayEquals(assertion.asDouble(), blank.asDouble(), 0.0001);
    }


    @Test
    public void testOffset() {
        DataBuffer create = Nd4j.createBuffer(new double[]{1,2,3,4},2);
        assertEquals(2,create.length());
        assertEquals(4,create.underlyingLength());
        assertEquals(2,create.offset());
        assertEquals(3,create.getDouble(0),1e-1);
        assertEquals(4,create.getDouble(1),1e-1);

    }



    @Override
    public char ordering() {
        return 'c';
    }
}
