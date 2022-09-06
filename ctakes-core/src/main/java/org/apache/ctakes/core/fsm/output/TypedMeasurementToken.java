/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ctakes.core.fsm.output;

import org.apache.ctakes.core.fsm.machine.MeasurementType;

/**
 * @author Mayo Clinic
 */
public class TypedMeasurementToken extends MeasurementToken {
    private MeasurementType measurementType;
    private int quantityStart;
    private int quantityEnd;
    private int unitStart;
    private int unitEnd;

    public TypedMeasurementToken(int startOffset, int endOffset, MeasurementType measurementType,
                                 int quantityStart, int quantityEnd, int unitStart, int unitEnd) {
        super(startOffset, endOffset);
        this.measurementType = measurementType;
        this.quantityStart = quantityStart;
        this.quantityEnd = quantityEnd;
        this.unitStart = unitStart;
        this.unitEnd = unitEnd;
    }

    public MeasurementType getMeasurementType() {
        return measurementType;
    }

    public void setMeasurementType(MeasurementType measurementType) {
        this.measurementType = measurementType;
    }

    public int getQuantityStart() {
        return quantityStart;
    }

    public void setQuantityStart(int quantityStart) {
        this.quantityStart = quantityStart;
    }

    public int getQuantityEnd() {
        return quantityEnd;
    }

    public void setQuantityEnd(int quantityEnd) {
        this.quantityEnd = quantityEnd;
    }

    public int getUnitStart() {
        return unitStart;
    }

    public void setUnitStart(int unitStart) {
        this.unitStart = unitStart;
    }

    public int getUnitEnd() {
        return unitEnd;
    }

    public void setUnitEnd(int unitEnd) {
        this.unitEnd = unitEnd;
    }
}
