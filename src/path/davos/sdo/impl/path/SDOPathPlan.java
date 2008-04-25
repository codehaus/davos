/*   Copyright 2008 BEA Systems Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package davos.sdo.impl.path;

import java.math.BigDecimal;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Jul 7, 2006
 */
public class SDOPathPlan
    implements PathPlan
{
    PathPlan _plan;
    Step _lastStep;

    SDOPathPlan()
    {
    }

    public boolean execute(Path context)
    {
        return _plan.execute(context);
    }

    public void optimize()
    {
        //nothig for now
    }

    public boolean isSimplePropertyName()
    {
        return _plan.isSimplePropertyName();
    }

    public String getSimplePropertyName()
    {
        return _plan.getSimplePropertyName();
    }

    PathPlan getPlan()
    {
        return _plan;
    }

    private void addStep(Step s)
    {
        if (_plan==null)
            _plan = s;
        else
            _lastStep.setChildStep(s);

        _lastStep = s;
    }

    void addRootStep()
    {
        addStep(new Step.RootStep());
    }

    void addPropertyStep(String property)
    {
        addStep(new Step.PropertyStep(property));
    }

    void addIndex0Step(String property, int index)
    {
        addStep(new Step.Index0Step(property, index));
    }

    void addIndex1Step(String property, int index)
    {
        addStep(new Step.Index1Step(property, index));
    }

    void addParentStep()
    {
        addStep(new Step.ParentStep());
    }

    void addLiteralStep(String property, String attribute, String value)
    {
        addStep(new Step.LiteralValueStep(property, attribute, value));
    }

    void addNumeralStep(String property, String attribute, BigDecimal value)
    {
        addStep(new Step.NumeralValueStep(property, attribute, value));
    }

    void addBooleanStep(String property, String attribute, boolean value)
    {
        addStep(new Step.BooleanValueStep(property, attribute, value));
    }

    public String toString()
    {
        return (_plan==null ? "no plan" : _plan.toString());
    }
}
