/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ctakes.core.fsm.machine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ctakes.core.fsm.condition.IntegerRangeCondition;
import org.apache.ctakes.core.fsm.condition.NumberCondition;
import org.apache.ctakes.core.fsm.condition.PunctuationValueCondition;
import org.apache.ctakes.core.fsm.condition.RangeCondition;
import org.apache.ctakes.core.fsm.condition.WordSetCondition;
import org.apache.ctakes.core.fsm.output.MeasurementToken;
import org.apache.ctakes.core.fsm.output.TypedMeasurementToken;
import org.apache.ctakes.core.fsm.state.NamedState;
import org.apache.ctakes.core.fsm.token.BaseToken;

import net.openai.util.fsm.AnyCondition;
import net.openai.util.fsm.Condition;
import net.openai.util.fsm.Machine;
import net.openai.util.fsm.State;

import static org.apache.ctakes.core.fsm.machine.MeasurementType.BLOOD_PRESSURE;
import static org.apache.ctakes.core.fsm.machine.MeasurementType.CONCENTRATION;
import static org.apache.ctakes.core.fsm.machine.MeasurementType.GENERAL_QUANTITY;

/**
 * Uses one or more finite state machines to detect measurements in the given
 * input of tokens.
 * 
 * @author Mayo Clinic
 */
public class MeasurementFSM {
	public static final String QUANTITY_STATE = "QUANTITY";
	public static final String UNIT_START = "UNIT_START";
	// text fractions
	Set<String> iv_fullTextSet = new HashSet<String>();
	Set<String> iv_shortTextSet = new HashSet<String>();
	Set<String> iv_textNumberSet = new HashSet<String>();

	Set<String> multiWordConcentrationUnitStartSet = new HashSet<>();
	Set<String> multiWordConcentrationUnitEndSet = new HashSet<>();

	// contains the finite state machines
	private Map<MeasurementType, Machine> iv_measurementToMachineMap = new HashMap<>();

	/**
	 * 
	 * Constructor
	 * 
	 */
	public MeasurementFSM() {
		iv_fullTextSet.add("gallon");
		iv_fullTextSet.add("gallons");
		iv_fullTextSet.add("pint");
		iv_fullTextSet.add("pints");
		iv_fullTextSet.add("ounce");
		iv_fullTextSet.add("ounces");
		iv_fullTextSet.add("pound");
		iv_fullTextSet.add("pounds");
		iv_fullTextSet.add("drop");
		iv_fullTextSet.add("drops");
		iv_fullTextSet.add("hour");
		iv_fullTextSet.add("hours");
		iv_fullTextSet.add("minute");
		iv_fullTextSet.add("minutes");
		iv_fullTextSet.add("second");
		iv_fullTextSet.add("seconds");
		iv_fullTextSet.add("foot");
		iv_fullTextSet.add("feet");
		iv_fullTextSet.add("grain");
		iv_fullTextSet.add("grains");
		iv_fullTextSet.add("teaspoon");
		iv_fullTextSet.add("teaspoons");
		iv_fullTextSet.add("tablespoon");
		iv_fullTextSet.add("tablespoons");
		iv_fullTextSet.add("kilogram");
		iv_fullTextSet.add("kilograms");
		iv_fullTextSet.add("gram");
		iv_fullTextSet.add("grams");
		iv_fullTextSet.add("centigram");
		iv_fullTextSet.add("centigrams");
		iv_fullTextSet.add("milligram");
		iv_fullTextSet.add("milligrams");
		iv_fullTextSet.add("liter");
		iv_fullTextSet.add("liters");
		iv_fullTextSet.add("centiliter");
		iv_fullTextSet.add("centiliters");
		iv_fullTextSet.add("milliliter");
		iv_fullTextSet.add("milliliters");
		iv_fullTextSet.add("meter");
		iv_fullTextSet.add("meters");
		iv_fullTextSet.add("centimeter");
		iv_fullTextSet.add("centimeters");
		iv_fullTextSet.add("millimeter");
		iv_fullTextSet.add("millimeters");
		iv_fullTextSet.add("day");
		iv_fullTextSet.add("days");

		iv_shortTextSet.add("gal");
		iv_shortTextSet.add("gals");
		iv_shortTextSet.add("pt");
		iv_shortTextSet.add("pts");
		iv_shortTextSet.add("oz");
		iv_shortTextSet.add("ozs");
		iv_shortTextSet.add("lb");
		iv_shortTextSet.add("lbs");
		iv_shortTextSet.add("gtts");
		iv_shortTextSet.add("hr");
		iv_shortTextSet.add("min");
		iv_shortTextSet.add("sec");
		iv_shortTextSet.add("ft");
		iv_shortTextSet.add("gr");
		iv_shortTextSet.add("tsp");
		iv_shortTextSet.add("tbsp");
		iv_shortTextSet.add("g");
		iv_shortTextSet.add("kg");
		iv_shortTextSet.add("mg");
		iv_shortTextSet.add("l");
		iv_shortTextSet.add("cl");
		iv_shortTextSet.add("ml");
		iv_shortTextSet.add("m");
		iv_shortTextSet.add("cm");
		iv_shortTextSet.add("mm");
		iv_shortTextSet.add("cc");
		iv_shortTextSet.add("h");

		iv_textNumberSet.add("one");
		iv_textNumberSet.add("two");
		iv_textNumberSet.add("three");
		iv_textNumberSet.add("four");
		iv_textNumberSet.add("five");
		iv_textNumberSet.add("six");
		iv_textNumberSet.add("seven");
		iv_textNumberSet.add("eight");
		iv_textNumberSet.add("nine");
		iv_textNumberSet.add("ten");

		multiWordConcentrationUnitStartSet.add("ng");
		multiWordConcentrationUnitStartSet.add("g");
		multiWordConcentrationUnitStartSet.add("µg");

		multiWordConcentrationUnitEndSet.add("l");
		multiWordConcentrationUnitEndSet.add("ml");

		iv_measurementToMachineMap.put(BLOOD_PRESSURE, getBloodPressureMachine());
		iv_measurementToMachineMap.put(GENERAL_QUANTITY, getSubstanceQuantityMachine());
		iv_measurementToMachineMap.put(CONCENTRATION, getConcentrationMachine());
	}

	/**
	 * Gets a finite state machine that detects the following:
	 * <ol>
	 * <li>110/80</li>
	 * </ol>
	 * 
	 * @return
	 */
	private Machine getBloodPressureMachine() {
		State startState = new NamedState("START");
		State endState = new NamedState("END");
		endState.setEndStateFlag(true);

		Machine m = new Machine(startState);
		State systolicState = new NamedState("SYSTOLIC");
		State fslashState = new NamedState("FSLASH");

		Condition systolicCondition = new IntegerRangeCondition(80, 200);
		Condition diastolicCondition = new IntegerRangeCondition(60, 160);
		Condition fslashCondition = new PunctuationValueCondition('/');

		startState.addTransition(systolicCondition, systolicState);
		startState.addTransition(new AnyCondition(), startState);

		systolicState.addTransition(fslashCondition, fslashState);
		systolicState.addTransition(new AnyCondition(), startState);

		fslashState.addTransition(diastolicCondition, endState);
		fslashState.addTransition(new AnyCondition(), startState);

		endState.addTransition(new AnyCondition(), startState);

		return m;
	}

	/**
	 * Gets a finite state machine that detects the following:
	 * <ol>
	 * <li>one teaspoon</li>
	 * <li>one tsp</li>
	 * <li>1 teaspoon</li>
	 * <li>1 tsp</li>
	 * <li>0.5 tsp</li>
	 * <li>1-5 teaspoons</li>
	 * </ol>
	 * 
	 * @return
	 */
	private Machine getSubstanceQuantityMachine() {
		State startState = new NamedState("START");
		State endState = new NamedState("END");
		endState.setEndStateFlag(true);

		Machine m = new Machine(startState);
		State quanitityState = new NamedState(QUANTITY_STATE);

		Condition numberCondition = new NumberCondition();
		Condition numberTextCondition = new WordSetCondition(iv_textNumberSet,
				false);
		Condition rangeCondition = new RangeCondition();
		Condition fullTextCondition = new WordSetCondition(iv_fullTextSet,
				false);
		Condition shortTextCondition = new WordSetCondition(iv_shortTextSet,
				false);

		startState.addTransition(numberCondition, quanitityState);
		startState.addTransition(rangeCondition, quanitityState);
		startState.addTransition(numberTextCondition, quanitityState);
		startState.addTransition(new AnyCondition(), startState);

		quanitityState.addTransition(fullTextCondition, endState);
		quanitityState.addTransition(shortTextCondition, endState);
		quanitityState.addTransition(new AnyCondition(), startState);

		endState.addTransition(new AnyCondition(), startState);

		return m;
	}

	private Machine getConcentrationMachine() {
		State startState = new NamedState("START");
		State endState = new NamedState("END");
		State fslashState = new NamedState("FSLASH");
		endState.setEndStateFlag(true);

		Machine m = new Machine(startState);
		State quanitityState = new NamedState(QUANTITY_STATE);
		State unitStartState = new NamedState(UNIT_START);

		Condition numberCondition = new NumberCondition();
		Condition numberTextCondition = new WordSetCondition(iv_textNumberSet,
				false);
		Condition rangeCondition = new RangeCondition();

		Condition unitStartCondition = new WordSetCondition(multiWordConcentrationUnitStartSet,
				false);
		Condition fslashCondition = new PunctuationValueCondition('/');
		Condition unitEndCondition = new WordSetCondition(multiWordConcentrationUnitEndSet,
				false);

		startState.addTransition(numberCondition, quanitityState);
		startState.addTransition(rangeCondition, quanitityState);
		startState.addTransition(numberTextCondition, quanitityState);
		startState.addTransition(new AnyCondition(), startState);

		quanitityState.addTransition(unitStartCondition, unitStartState);
		quanitityState.addTransition(new AnyCondition(), startState);

		unitStartState.addTransition(fslashCondition, fslashState);
		unitStartState.addTransition(new AnyCondition(), startState);

		fslashState.addTransition(unitEndCondition, endState);
		fslashState.addTransition(new AnyCondition(), startState);

		endState.addTransition(new AnyCondition(), startState);

		return m;
	}

	/**
	 * Executes the finite state machines.
	 * 
	 * @return Set of RangeToken objects.
	 */
	public Set<MeasurementToken> execute(List<? extends BaseToken> tokens,
			Set<? extends BaseToken> overrideSet) throws Exception {
		Set<MeasurementToken> measurementSet = new HashSet<MeasurementToken>();

		// maps a fsm to a token start index
		// key = fsm , value = token start index
		Map<Machine, Integer> tokenStartMap = new HashMap<Machine, Integer>();
		Map<Machine, Integer> tokenQuantityEndMap = new HashMap<Machine, Integer>();
		Map<Machine, Integer> tokenUnitStartMap = new HashMap<Machine, Integer>();

		Iterator<? extends BaseToken> overrideTokenItr = overrideSet.iterator();
		// key = start offset, value = override BaseToken object
		Map<Integer, BaseToken> overrideTokenMap = new HashMap<Integer, BaseToken>();
		while (overrideTokenItr.hasNext()) {
			BaseToken t = overrideTokenItr.next();
			Integer key = new Integer(t.getStartOffset());
			overrideTokenMap.put(key, t);
		}

		boolean overrideOn = false;
		int overrideEndOffset = -1;
		for (int i = 0; i < tokens.size(); i++) {
			BaseToken token = tokens.get(i);

			Integer key = new Integer(token.getStartOffset());

			if (overrideOn) {
				if (token.getStartOffset() >= overrideEndOffset) {
					overrideOn = false;
					overrideEndOffset = -1;
				} else {
					// step to next iteration of for loop
					continue;
				}
			} else {
				if (overrideTokenMap.containsKey(key)) {
					// override one or more tokens until the override
					// token is complete
					token = overrideTokenMap.get(key);
					overrideOn = true;
					overrideEndOffset = token.getEndOffset();
				}
			}

			Iterator<Map.Entry<MeasurementType, Machine>> machineItr = iv_measurementToMachineMap.entrySet().iterator();
			while (machineItr.hasNext()) {
				Map.Entry<MeasurementType, Machine> entry = machineItr.next();
				Machine fsm = entry.getValue();
				MeasurementType measurementType = entry.getKey();

				fsm.input(token);

				State currentState = fsm.getCurrentState();
				if (currentState.getStartStateFlag()) {
					tokenStartMap.put(fsm, new Integer(i));
				}

				if (currentState.getName().equals(QUANTITY_STATE)){
					tokenQuantityEndMap.put(fsm,i);
				}
				if (currentState.getName().equals(UNIT_START)){
					tokenUnitStartMap.put(fsm, i);
				}

				if (currentState.getEndStateFlag()) {
					Object o = tokenStartMap.get(fsm);
					int tokenStartIndex;
					if (o == null) {
						// By default, all machines start with
						// token zero.
						tokenStartIndex = 0;
					} else {
						tokenStartIndex = ((Integer) o).intValue();
						// skip ahead over single token we don't want
						tokenStartIndex++;
					}
					BaseToken startToken = tokens.get(tokenStartIndex);
					BaseToken endToken = token;

					int quantityStart = 0;
					int quantityEnd = 0;
					int unitStart = 0;
					int unitEnd = 0;
					if (measurementType == GENERAL_QUANTITY || measurementType == CONCENTRATION) {
						quantityStart = startToken.getStartOffset();

						int endTokenQuantityIndex = tokenQuantityEndMap.get(fsm);
						quantityEnd = tokens.get(endTokenQuantityIndex).getEndOffset();

						unitEnd = endToken.getEndOffset();

						if (measurementType == GENERAL_QUANTITY) {
							unitStart = endToken.getStartOffset();
						}
						if (measurementType == CONCENTRATION) {
							int startTokenUnitIndex = tokenUnitStartMap.get(fsm);
							unitStart = tokens.get(startTokenUnitIndex).getStartOffset();
						}
					}

					TypedMeasurementToken measurementToken = new TypedMeasurementToken(
							startToken.getStartOffset(), endToken.getEndOffset(), measurementType,
							quantityStart, quantityEnd, unitStart, unitEnd);
					measurementSet.add(measurementToken);
					fsm.reset();
				}
			}
		}

		// cleanup
		tokenStartMap.clear();
		tokenQuantityEndMap.clear();
		tokenUnitStartMap.clear();

		// reset machines
		Iterator<Machine> itr = iv_measurementToMachineMap.values().iterator();
		while (itr.hasNext()) {
			Machine fsm = itr.next();
			fsm.reset();
		}

		return measurementSet;
	}
}
