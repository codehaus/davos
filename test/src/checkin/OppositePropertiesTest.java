/*   Copyright 2008 BEA Systems, Inc.
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
 *   limitations under the License.
 */
package checkin;

import common.BaseTest;
import javax.sdo.helper.TypeHelper;
import javax.sdo.helper.DataFactory;
import javax.sdo.Type;
import javax.sdo.DataObject;
import javax.sdo.Property;
import javax.sdo.Sequence;
import davos.sdo.impl.type.BuiltInTypeSystem;

import java.util.Arrays;

/**
 * @author Cezar Andrei (cezar.andrei at bea.com)
 *         Date: Sep 20, 2006
 */
public class OppositePropertiesTest
    extends BaseTest
{
    public OppositePropertiesTest(String name)
    {
        super(name);
    }

    private static DataFactory dataFactory = context.getDataFactory();
    private static TypeHelper typeHelper = context.getTypeHelper();

    private static final String URI = "checkin.OppositePropertiesTest";
    private static final String URInProps = "checkin.OppositePropertiesTest.NullableProps";
    private static boolean typesCreated = false;
    private static boolean typesWithNullablePropsCreated = false;

    public static void testOppositeDynamic()
    {
        Type stringType = typeHelper.getType("commonj.sdo", "String");

        // create a new Type for Person
        DataObject personTypeDescriptor = dataFactory.create("commonj.sdo", "Type");
        personTypeDescriptor.set("uri", URI);
        personTypeDescriptor.set("name", "Person");
        personTypeDescriptor.setBoolean("dataType", false);        //default
        personTypeDescriptor.setBoolean("open", false);        //default
        personTypeDescriptor.setBoolean("sequenced", false);        //default

        // Person.name
        DataObject namePProperty = personTypeDescriptor.createDataObject("property");
        namePProperty.set("name", "name");
        namePProperty.set("type", stringType);
        namePProperty.setBoolean("many", false);        //default
        namePProperty.setBoolean("containment", true);


        // create a new Type for House
        DataObject houseTypeDescriptor = dataFactory.create("commonj.sdo", "Type");
        houseTypeDescriptor.set("uri", URI);
        houseTypeDescriptor.set("name", "House");
        houseTypeDescriptor.setBoolean("dataType", false);        //default
        houseTypeDescriptor.setBoolean("open", false);        //default
        houseTypeDescriptor.setBoolean("sequenced", false);        //default

        // House.color
        DataObject colorHProperty = houseTypeDescriptor.createDataObject("property");
        colorHProperty.set("name", "color");
        colorHProperty.set("type", stringType);
        colorHProperty.setBoolean("many", false);        //default
        colorHProperty.setBoolean("containment", true);

        // Person.owns
        DataObject ownsPProperty = personTypeDescriptor.createDataObject("property");
        ownsPProperty.set("name", "owns");
        ownsPProperty.set("type", houseTypeDescriptor);
        ownsPProperty.setBoolean("many", false);        //default
        ownsPProperty.setBoolean("containment", true);

        // House.ownedBy
        DataObject ownedByHProperty = houseTypeDescriptor.createDataObject("property");
        ownedByHProperty.set("name", "ownedBy");
        ownedByHProperty.set("type", personTypeDescriptor);
        ownedByHProperty.setBoolean("many", false);        //default
        ownedByHProperty.setBoolean("containment", false);        //default
        ownedByHProperty.set("opposite", ownsPProperty);

        ownsPProperty.set("opposite", ownedByHProperty);

        // Person.visited
        DataObject visitedPProperty = personTypeDescriptor.createDataObject("property");
        visitedPProperty.set("name", "visited");
        visitedPProperty.set("type", houseTypeDescriptor);
        visitedPProperty.setBoolean("many", true);
        visitedPProperty.setBoolean("containment", true);

        // House.visitedBy
        DataObject visitedByHProperty = houseTypeDescriptor.createDataObject("property");
        visitedByHProperty.set("name", "visitedBy");
        visitedByHProperty.set("type", personTypeDescriptor);
        ownedByHProperty.setBoolean("many", false);        //default
        ownedByHProperty.setBoolean("containment", false);        //default

        visitedByHProperty.set("opposite", visitedPProperty);
        visitedPProperty.set("opposite", visitedByHProperty);

        // define the types
        Type persType = typeHelper.define(personTypeDescriptor);


        //BindingSystem bs = SDOContextFactory.getDefaultBindingSystem();
        //TypeSystem ts = SDOContextFactory.getDefaultSDOTypeSystem();
        //((TypeSystemBase)ts).dumpWithoutBuiltinTypes();
        //((DynamicBindingSystem)bs).dump();

        Property ownsP = persType.getProperty("owns");
        Property visitedP = persType.getProperty("visited");
        Type houseType = ownsP.getType();
        Property ownedByH = houseType.getProperty("ownedBy");
        Property visitedByH = houseType.getProperty("visitedBy");

        assertEquals(true, ownsP.getOpposite()==ownedByH);
        assertEquals(true, visitedP.getOpposite()==visitedByH);
        assertEquals(true, ownedByH.getOpposite()==ownsP);
        assertEquals(true, visitedByH.getOpposite()==visitedP);


        DataObject person = dataFactory.create(persType);
        person.set("name", "John");
        // a create will set both references
        DataObject house = person.createDataObject("owns");
        house.set("color" , "red");
        //printDO(person);
        //System.out.println("  person.getDataObject(\"owns\").get(\"color\") : " + person.getDataObject("owns").get("color"));
        //System.out.println("  person.getDataObject(\"owns\").getDataObject(\"ownedBy\").get(\"name\") : " + person.getDataObject("owns").getDataObject("ownedBy").get("name"));
        assertEquals("red", person.getDataObject("owns").get("color"));
        assertEquals("John", person.getDataObject("owns").getDataObject("ownedBy").get("name"));


        // unset will remove both references
        house.unset(ownedByH);
        //System.out.println("\n\nhouse.unset(ownedByH):");
        //printDO(person);
        //printDO(house);
        assertEquals(null, person.getDataObject(ownsP));
        assertEquals(null, house.getDataObject(ownedByH));


        // set will set both references
        house.set(ownedByH, person);
        //System.out.println("\n\nhouse.set(ownedByH, person):");
        //printDO(person);
        assertEquals("red",  person.getDataObject("owns").get("color"));
        assertEquals("John", person.getDataObject("owns").getDataObject("ownedBy").get("name"));
        assertEquals("John", house.getDataObject("ownedBy").get("name"));
        assertEquals("red",  house.getDataObject("ownedBy").getDataObject("owns").get("color"));


        // the other unset does the same thing
        person.unset(ownsP);
        //System.out.println("\n\nperson.unset(ownsP):");
        //printDO(person);
        //printDO(house);
        assertEquals(null, person.getDataObject(ownsP));
        assertEquals(null, house.getDataObject(ownedByH));


        // Same tests, but this time "visited" is a many Property
        // create
        //System.out.println("\n\nperson.createDO(visited) blue+green:");
        DataObject visitedHouse1 = person.createDataObject("visited");
        visitedHouse1.set("color" , "blue");
        DataObject visitedHouse2 = person.createDataObject("visited");
        visitedHouse2.set("color" , "green");
        //printDO(person);
        assertEquals("blue",  person.getDataObject("visited.0").get("color"));
        assertEquals("green", person.getDataObject("visited.1").get("color"));
        assertEquals(true, visitedHouse1 == person.getDataObject("visited.0"));
        assertEquals(true, visitedHouse2 == person.getDataObject("visited.1"));
        assertEquals("John",  person.getDataObject("visited.0").getDataObject("visitedBy").get("name"));
        assertEquals("John",  person.getDataObject("visited.1").getDataObject("visitedBy").get("name"));
        assertEquals(true, person == visitedHouse1.getDataObject("visitedBy"));
        assertEquals(true, person == visitedHouse2.getDataObject("visitedBy"));


        // unset
        //System.out.println("\n\nperson.unset(visited)");
        person.unset(visitedP);
        //printDO(person);
        assertEquals(0, person.getList("visited").size());
        assertEquals(null, visitedHouse1.getDataObject("visitedBy"));
        assertEquals(null, visitedHouse2.getDataObject("visitedBy"));
    }


    public void createTypes()
    {
        if (typesCreated)
            return;

        typesCreated = true;

        DataObject defT1 = dataFactory.create(BuiltInTypeSystem.TYPE);
        defT1.set("name", "T1");
        defT1.set("uri", URI);
        defT1.setBoolean("sequenced", true);

        DataObject defT2 = dataFactory.create(BuiltInTypeSystem.TYPE);
        defT2.set("name", "T2");
        defT2.set("uri", URI);
        defT2.setBoolean("sequenced", true);

        DataObject defT3 = dataFactory.create(BuiltInTypeSystem.TYPE);
        defT3.set("name", "T3");
        defT3.set("uri", URI);
        defT3.setBoolean("sequenced", true);

        // T1 - T2 props, one end is always contaiment
        // create T1 -> T2 prop single -> single
        DataObject dpOppoT1T2_ContainmentSingle = defT1.createDataObject("property");
        dpOppoT1T2_ContainmentSingle.set("name", "pOppoT1T2_ContainmentSingle");
        dpOppoT1T2_ContainmentSingle.set("type", defT2);
        dpOppoT1T2_ContainmentSingle.setBoolean("containment", true);

        // create T2 -> T1 prop single -> single
        DataObject dpOppoT2T1_NonContainmentSingle = defT2.createDataObject("property");
        dpOppoT2T1_NonContainmentSingle.set("name", "pOppoT2T1_NonContainmentSingle");
        dpOppoT2T1_NonContainmentSingle.set("type", defT1);

        // set as opposites
        dpOppoT1T2_ContainmentSingle.set("opposite", dpOppoT2T1_NonContainmentSingle);
        dpOppoT2T1_NonContainmentSingle.set("opposite", dpOppoT1T2_ContainmentSingle);

        // create T1 -> T2 prop many -> single
        DataObject dpOppoT1T2_ContainmentMany = defT1.createDataObject("property");
        dpOppoT1T2_ContainmentMany.set("name", "pOppoT1T2_ContainmentMany");
        dpOppoT1T2_ContainmentMany.set("type", defT2);
        dpOppoT1T2_ContainmentMany.setBoolean("containment", true);
        dpOppoT1T2_ContainmentMany.setBoolean("many", true);

        // create T2 -> T1 prop single -> many
        DataObject dpOppoT2T1_NonContainmentSingle2 = defT2.createDataObject("property");
        dpOppoT2T1_NonContainmentSingle2.set("name", "pOppoT2T1_NonContainmentSingle2");
        dpOppoT2T1_NonContainmentSingle2.set("type", defT1);

        // set as opposites
        dpOppoT1T2_ContainmentMany.set("opposite", dpOppoT2T1_NonContainmentSingle2);
        dpOppoT2T1_NonContainmentSingle2.set("opposite", dpOppoT1T2_ContainmentMany);


        // T1 - T3 props, both ends are non-containment
        // create T1 -> T3 prop single -> single
        DataObject dpOppoT1T3_NonContainmentSingle = defT1.createDataObject("property");
        dpOppoT1T3_NonContainmentSingle.set("name", "pOppoT1T3_NonContainmentSingle");
        dpOppoT1T3_NonContainmentSingle.set("type", defT3);

        // create T3 -> T1 prop single -> single
        DataObject dpOppoT3T1_NonContainmentSingle = defT3.createDataObject("property");
        dpOppoT3T1_NonContainmentSingle.set("name", "pOppoT3T1_NonContainmentSingle");
        dpOppoT3T1_NonContainmentSingle.set("type", defT1);

        // set as opposites
        dpOppoT1T3_NonContainmentSingle.set("opposite", dpOppoT3T1_NonContainmentSingle);
        dpOppoT3T1_NonContainmentSingle.set("opposite", dpOppoT1T3_NonContainmentSingle);

        // create T1 -> T3 prop many -> many
        DataObject dpOppoT1T3_NonContainmentMany = defT1.createDataObject("property");
        dpOppoT1T3_NonContainmentMany.set("name", "pOppoT1T3_NonContainmentMany");
        dpOppoT1T3_NonContainmentMany.set("type", defT3);
        dpOppoT1T3_NonContainmentMany.setBoolean("many", true);

        // create T3 -> T1 prop many -> many
        DataObject dpOppoT3T1_NonContainmentMany = defT3.createDataObject("property");
        dpOppoT3T1_NonContainmentMany.set("name", "pOppoT3T1_NonContainmentMany");
        dpOppoT3T1_NonContainmentMany.set("type", defT1);
        dpOppoT3T1_NonContainmentMany.setBoolean("many", true);

        // set as opposites
        dpOppoT1T3_NonContainmentMany.set("opposite", dpOppoT3T1_NonContainmentMany);
        dpOppoT3T1_NonContainmentMany.set("opposite", dpOppoT1T3_NonContainmentMany);


        typeHelper.define(Arrays.asList(new DataObject[] {defT1, defT2, defT3}));

        // check if define worked fine
        Type t1 = typeHelper.getType(URI, "T1");
        Type t2 = typeHelper.getType(URI, "T2");
        Type t3 = typeHelper.getType(URI, "T3");

        // single
        Property pOppoT1T2_ContainmentSingle = t1.getProperty("pOppoT1T2_ContainmentSingle");
        Property pOppoT2T1_NonContainmentSingle = t2.getProperty("pOppoT2T1_NonContainmentSingle");

        Property pOppoT1T3_NonContainmentSingle = t1.getProperty("pOppoT1T3_NonContainmentSingle");
        Property pOppoT3T1_NonContainmentSingle = t3.getProperty("pOppoT3T1_NonContainmentSingle");

        assert pOppoT1T2_ContainmentSingle    != null;
        assert pOppoT2T1_NonContainmentSingle != null;
        assert pOppoT1T3_NonContainmentSingle != null;
        assert pOppoT3T1_NonContainmentSingle != null;

        assert pOppoT1T2_ContainmentSingle.getOpposite()    == pOppoT2T1_NonContainmentSingle;
        assert pOppoT2T1_NonContainmentSingle.getOpposite() == pOppoT1T2_ContainmentSingle;

        assert pOppoT1T3_NonContainmentSingle.getOpposite() == pOppoT3T1_NonContainmentSingle;
        assert pOppoT3T1_NonContainmentSingle.getOpposite() == pOppoT1T3_NonContainmentSingle;

        // many
        Property pOppoT1T2_ContainmentMany = t1.getProperty("pOppoT1T2_ContainmentMany");
        Property pOppoT2T1_NonContainmentSingle2 = t2.getProperty("pOppoT2T1_NonContainmentSingle2");

        Property pOppoT1T3_NonContainmentMany = t1.getProperty("pOppoT1T3_NonContainmentMany");
        Property pOppoT3T1_NonContainmentMany = t3.getProperty("pOppoT3T1_NonContainmentMany");

        assert pOppoT1T2_ContainmentMany    != null;
        assert pOppoT2T1_NonContainmentSingle2 != null;
        assert pOppoT1T3_NonContainmentMany != null;
        assert pOppoT3T1_NonContainmentMany != null;

        assert pOppoT1T2_ContainmentMany.getOpposite()    == pOppoT2T1_NonContainmentSingle2;
        assert pOppoT2T1_NonContainmentSingle2.getOpposite() == pOppoT1T2_ContainmentMany;

        assert pOppoT1T3_NonContainmentMany.getOpposite() == pOppoT3T1_NonContainmentMany;
        assert pOppoT3T1_NonContainmentMany.getOpposite() == pOppoT1T3_NonContainmentMany;
    }

    /* containment single <-> non-containment single */
    public void testOppoSingleContainment()
    {
        createTypes();

        Type t1 = typeHelper.getType(URI, "T1");
        Type t2 = typeHelper.getType(URI, "T2");

        Property pOppoT1T2_ContainmentSingle = t1.getProperty("pOppoT1T2_ContainmentSingle");
        Property pOppoT2T1_NonContainmentSingle = t2.getProperty("pOppoT2T1_NonContainmentSingle");

        // Contaiment Single
        DataObject o1 = dataFactory.create(t1);
        DataObject o2 = o1.createDataObject(pOppoT1T2_ContainmentSingle);

        assertNotNull(o2);

        assertEquals(o2, o1.getDataObject(pOppoT1T2_ContainmentSingle));
        assertEquals(o1, o2.getDataObject(pOppoT2T1_NonContainmentSingle));

        o1.unset(pOppoT1T2_ContainmentSingle);
        assertEquals(false, o1.isSet(pOppoT1T2_ContainmentSingle));
        assertEquals(false, o2.isSet(pOppoT2T1_NonContainmentSingle));
        assertNull(o1.getDataObject(pOppoT1T2_ContainmentSingle));
        assertNull(o2.getDataObject(pOppoT2T1_NonContainmentSingle));

        o1.set(pOppoT1T2_ContainmentSingle, o2);
        assertEquals(o2, o1.getDataObject(pOppoT1T2_ContainmentSingle));
        assertEquals(o1, o2.getDataObject(pOppoT2T1_NonContainmentSingle));

        o2.unset(pOppoT2T1_NonContainmentSingle);
        assertEquals(false, o1.isSet(pOppoT1T2_ContainmentSingle));
        assertEquals(false, o2.isSet(pOppoT2T1_NonContainmentSingle));
        assertNull(o1.getDataObject(pOppoT1T2_ContainmentSingle));
        assertNull(o2.getDataObject(pOppoT2T1_NonContainmentSingle));

        o2.set(pOppoT2T1_NonContainmentSingle, o1);
        assertEquals(o2, o1.getDataObject(pOppoT1T2_ContainmentSingle));
        assertEquals(o1, o2.getDataObject(pOppoT2T1_NonContainmentSingle));
    }

    /* non-containment single <-> non-containment single */
    public void testOppoSingleNonContainment()
    {
        createTypes();

        Type t1 = typeHelper.getType(URI, "T1");
        Type t3 = typeHelper.getType(URI, "T3");

        Property pOppoT1T3_NonContainmentSingle = t1.getProperty("pOppoT1T3_NonContainmentSingle");
        Property pOppoT3T1_NonContainmentSingle = t3.getProperty("pOppoT3T1_NonContainmentSingle");

        DataObject o1 = dataFactory.create(t1);
        DataObject o3 = o1.createDataObject(pOppoT1T3_NonContainmentSingle);

        assertNotNull(o3);

        assertEquals(o3, o1.getDataObject(pOppoT1T3_NonContainmentSingle));
        assertEquals(o1, o3.getDataObject(pOppoT3T1_NonContainmentSingle));

        o1.unset(pOppoT1T3_NonContainmentSingle);
        assertEquals(false, o1.isSet(pOppoT1T3_NonContainmentSingle));
        assertEquals(false, o3.isSet(pOppoT3T1_NonContainmentSingle));
        assertNull(o1.getDataObject(pOppoT1T3_NonContainmentSingle));
        assertNull(o3.getDataObject(pOppoT3T1_NonContainmentSingle));

        o1.set(pOppoT1T3_NonContainmentSingle, o3);
        assertEquals(o3, o1.getDataObject(pOppoT1T3_NonContainmentSingle));
        assertEquals(o1, o3.getDataObject(pOppoT3T1_NonContainmentSingle));

        o3.unset(pOppoT3T1_NonContainmentSingle);
        assertEquals(false, o1.isSet(pOppoT1T3_NonContainmentSingle));
        assertEquals(false, o3.isSet(pOppoT3T1_NonContainmentSingle));
        assertNull(o1.getDataObject(pOppoT1T3_NonContainmentSingle));
        assertNull(o3.getDataObject(pOppoT3T1_NonContainmentSingle));

        o3.set(pOppoT3T1_NonContainmentSingle, o1);
        assertEquals(o3, o1.getDataObject(pOppoT1T3_NonContainmentSingle));
        assertEquals(o1, o3.getDataObject(pOppoT3T1_NonContainmentSingle));
    }

    /* containment many <-> non-containment single */
    public void testOppoManyContainment()
    {
        createTypes();

        Type t1 = typeHelper.getType(URI, "T1");
        Type t2 = typeHelper.getType(URI, "T2");

        Property pOppoT1T2_ContainmentMany = t1.getProperty("pOppoT1T2_ContainmentMany");
        Property pOppoT2T1_NonContainmentSingle2 = t2.getProperty("pOppoT2T1_NonContainmentSingle2");

        DataObject o1 = dataFactory.create(t1);
        // contaiment create
        DataObject o2 = o1.createDataObject(pOppoT1T2_ContainmentMany);

        assertNotNull(o2);

        assertEquals(1, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(o2, o1.getList(pOppoT1T2_ContainmentMany).get(0));
        assertEquals(o1, o2.getDataObject(pOppoT2T1_NonContainmentSingle2));

        // unset
        o1.unset(pOppoT1T2_ContainmentMany);
        assertEquals(0, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(false, o2.isSet(pOppoT2T1_NonContainmentSingle2));
        assertNull(o2.getDataObject(pOppoT2T1_NonContainmentSingle2));

        // set
        o1.set(pOppoT1T2_ContainmentMany, o2);
        assertEquals(1, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(o2, o1.getList(pOppoT1T2_ContainmentMany).get(0));
        assertEquals(o1, o2.getDataObject(pOppoT2T1_NonContainmentSingle2));

        // list remove
        o1.getList(pOppoT1T2_ContainmentMany).remove(0);
        assertEquals(0, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(false, o2.isSet(pOppoT2T1_NonContainmentSingle2));
        assertEquals(null, o2.getDataObject(pOppoT2T1_NonContainmentSingle2));

        // list add
        o1.getList(pOppoT1T2_ContainmentMany).add(o2);
        assertEquals(1, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(o2, o1.getList(pOppoT1T2_ContainmentMany).get(0));
        assertEquals(o1, o2.getDataObject(pOppoT2T1_NonContainmentSingle2));

        // list remove
        o1.getList(pOppoT1T2_ContainmentMany).remove(0);
        assertEquals(0, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(false, o2.isSet(pOppoT2T1_NonContainmentSingle2));
        assertEquals(null, o2.getDataObject(pOppoT2T1_NonContainmentSingle2));


        // the other set
        o2.set(pOppoT2T1_NonContainmentSingle2, o1);
        assertEquals(1, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(o2, o1.getList(pOppoT1T2_ContainmentMany).get(0));
        assertEquals(o1, o2.getDataObject(pOppoT2T1_NonContainmentSingle2));

        // the other unset
        o2.unset(pOppoT2T1_NonContainmentSingle2);
        assertEquals(0, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(false, o2.isSet(pOppoT2T1_NonContainmentSingle2));
        assertEquals(null, o2.getDataObject(pOppoT2T1_NonContainmentSingle2));


        // sequence add
        Sequence o1seq = o1.getSequence();
        o1seq.add(pOppoT1T2_ContainmentMany, o2);
        assertEquals(1, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(o2, o1.getList(pOppoT1T2_ContainmentMany).get(0));
        assertEquals(o1, o2.getDataObject(pOppoT2T1_NonContainmentSingle2));
        assertEquals(1, o1seq.size());
        assertEquals(pOppoT1T2_ContainmentMany, o1seq.getProperty(0));
        assertEquals(o2, o1seq.getValue(0));

        Sequence o2seq = o2.getSequence();
        assertEquals(1, o2seq.size());
        assertEquals(pOppoT2T1_NonContainmentSingle2, o2seq.getProperty(0));
        assertEquals(o1, o2seq.getValue(0));

        // sequence remove
        o1seq.remove(0);
        assertEquals(0, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(null, o2.getDataObject(pOppoT2T1_NonContainmentSingle2));
        assertEquals(0, o1seq.size());
        assertEquals(0, o2seq.size());


        // the other sequence add
        o2seq.add(pOppoT2T1_NonContainmentSingle2, o1);

        assertEquals(1, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(o2, o1.getList(pOppoT1T2_ContainmentMany).get(0));
        assertEquals(o1, o2.getDataObject(pOppoT2T1_NonContainmentSingle2));
        assertEquals(1, o1seq.size());
        assertEquals(pOppoT1T2_ContainmentMany, o1seq.getProperty(0));
        assertEquals(o2, o1seq.getValue(0));

        assertEquals(1, o2seq.size());
        assertEquals(pOppoT2T1_NonContainmentSingle2, o2seq.getProperty(0));
        assertEquals(o1, o2seq.getValue(0));


        // the other sequence remove
        o2seq.remove(0);
        assertEquals(0, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(false, o2.isSet(pOppoT2T1_NonContainmentSingle2));
        assertEquals(null, o2.getDataObject(pOppoT2T1_NonContainmentSingle2));
        assertEquals(0, o1seq.size());
        assertEquals(0, o2seq.size());
    }

    /* non-containment many <-> non-containment many */
    public void testOppoManyNonContainment()
    {
        createTypes();

        Type t1 = typeHelper.getType(URI, "T1");
        Type t3 = typeHelper.getType(URI, "T3");

        Property pOppoT1T3_NonContainmentMany = t1.getProperty("pOppoT1T3_NonContainmentMany");
        Property pOppoT3T1_NonContainmentMany = t3.getProperty("pOppoT3T1_NonContainmentMany");

        DataObject o1 = dataFactory.create(t1);
        DataObject o3 = dataFactory.create(t3);

        assertNotNull(o3);

        assertEquals(0, o1.getList(pOppoT1T3_NonContainmentMany).size());
        assertEquals(0, o3.getList(pOppoT3T1_NonContainmentMany).size());

        // set
        o1.set(pOppoT1T3_NonContainmentMany, o3);
        assertEquals(1, o1.getList(pOppoT1T3_NonContainmentMany).size());
        assertEquals(o3, o1.getList(pOppoT1T3_NonContainmentMany).get(0));
        assertEquals(1, o3.getList(pOppoT3T1_NonContainmentMany).size());
        assertEquals(o1, o3.getList(pOppoT3T1_NonContainmentMany).get(0));

        // list remove
        o1.getList(pOppoT1T3_NonContainmentMany).remove(0);
        assertEquals(0, o1.getList(pOppoT1T3_NonContainmentMany).size());
        assertEquals(0, o3.getList(pOppoT3T1_NonContainmentMany).size());

        // list add
        o1.getList(pOppoT1T3_NonContainmentMany).add(o3);
        assertEquals(1, o1.getList(pOppoT1T3_NonContainmentMany).size());
        assertEquals(o3, o1.getList(pOppoT1T3_NonContainmentMany).get(0));
        assertEquals(1, o3.getList(pOppoT3T1_NonContainmentMany).size());
        assertEquals(o1, o3.getList(pOppoT3T1_NonContainmentMany).get(0));

        // list remove
        o1.getList(pOppoT1T3_NonContainmentMany).remove(0);
        assertEquals(false, o1.isSet(pOppoT1T3_NonContainmentMany));
        assertEquals(0, o1.getList(pOppoT1T3_NonContainmentMany).size());
        assertEquals(false, o3.isSet(pOppoT3T1_NonContainmentMany));
        assertEquals(0, o3.getList(pOppoT3T1_NonContainmentMany).size());
        //todo add tests for list.add(index, value), list.remove(object), list.set(index, value)


        // the other set
        o3.set(pOppoT3T1_NonContainmentMany, o1);
        assertEquals(1, o1.getList(pOppoT1T3_NonContainmentMany).size());
        assertEquals(o3, o1.getList(pOppoT1T3_NonContainmentMany).get(0));
        assertEquals(1, o3.getList(pOppoT3T1_NonContainmentMany).size());
        assertEquals(o1, o3.getList(pOppoT3T1_NonContainmentMany).get(0));

        // the other unset
        o3.unset(pOppoT3T1_NonContainmentMany);
        assertEquals(false, o1.isSet(pOppoT1T3_NonContainmentMany));
        assertEquals(0, o1.getList(pOppoT1T3_NonContainmentMany).size());
        assertEquals(false, o3.isSet(pOppoT3T1_NonContainmentMany));
        assertEquals(0, o3.getList(pOppoT3T1_NonContainmentMany).size());



        // sequence add
        Sequence o1seq = o1.getSequence();
        o1seq.add(pOppoT1T3_NonContainmentMany, o3);
        assertEquals(1, o1.getList(pOppoT1T3_NonContainmentMany).size());
        assertEquals(o3, o1.getList(pOppoT1T3_NonContainmentMany).get(0));
        assertEquals(1, o3.getList(pOppoT3T1_NonContainmentMany).size());
        assertEquals(o1, o3.getList(pOppoT3T1_NonContainmentMany).get(0));
        assertEquals(1, o1seq.size());
        assertEquals(pOppoT1T3_NonContainmentMany, o1seq.getProperty(0));
        assertEquals(o3, o1seq.getValue(0));

        Sequence o3seq = o3.getSequence();
        assertEquals(1, o3seq.size());
        assertEquals(pOppoT3T1_NonContainmentMany, o3seq.getProperty(0));
        assertEquals(o1, o3seq.getValue(0));


        // sequence remove
        o1seq.remove(0);
        assertEquals(0, o1.getList(pOppoT1T3_NonContainmentMany).size());
        assertEquals(0, o3.getList(pOppoT3T1_NonContainmentMany).size());
        assertEquals(0, o1seq.size());
        assertEquals(0, o3seq.size());

        assertEquals(false, o1.isSet(pOppoT1T3_NonContainmentMany));
        assertEquals(false, o3.isSet(pOppoT3T1_NonContainmentMany));


        // the other sequence add
        o3seq.add(pOppoT3T1_NonContainmentMany, o1);

        assertEquals(1, o1.getList(pOppoT1T3_NonContainmentMany).size());
        assertEquals(o3, o1.getList(pOppoT1T3_NonContainmentMany).get(0));
        assertEquals(1, o3.getList(pOppoT3T1_NonContainmentMany).size());
        assertEquals(o1, o3.getList(pOppoT3T1_NonContainmentMany).get(0));
        assertEquals(1, o1seq.size());
        assertEquals(pOppoT1T3_NonContainmentMany, o1seq.getProperty(0));
        assertEquals(o3, o1seq.getValue(0));

        assertEquals(1, o3seq.size());
        assertEquals(pOppoT3T1_NonContainmentMany, o3seq.getProperty(0));
        assertEquals(o1, o3seq.getValue(0));


        // the other sequence remove
        o3seq.remove(0);
        assertEquals(0, o1.getList(pOppoT1T3_NonContainmentMany).size());
        assertEquals(0, o3.getList(pOppoT3T1_NonContainmentMany).size());
        assertEquals(0, o1seq.size());
        assertEquals(0, o3seq.size());

        assertEquals(false, o1.isSet(pOppoT1T3_NonContainmentMany));
        assertEquals(false, o3.isSet(pOppoT3T1_NonContainmentMany));
    }

    public void createTypesWithNullableProps()
    {
        if (typesWithNullablePropsCreated)
            return;

        typesWithNullablePropsCreated = true;

        DataObject defT1 = dataFactory.create(BuiltInTypeSystem.TYPE);
        defT1.set("name", "T1");
        defT1.set("uri", URInProps);
        defT1.setBoolean("sequenced", true);

        DataObject defT2 = dataFactory.create(BuiltInTypeSystem.TYPE);
        defT2.set("name", "T2");
        defT2.set("uri", URInProps);
        defT2.setBoolean("sequenced", true);

        DataObject defT3 = dataFactory.create(BuiltInTypeSystem.TYPE);
        defT3.set("name", "T3");
        defT3.set("uri", URInProps);
        defT3.setBoolean("sequenced", true);

        // T1 - T2 props, one end is always contaiment
        // create T1 -> T2 prop single -> single
        DataObject dpOppoT1T2_ContainmentSingle = defT1.createDataObject("property");
        dpOppoT1T2_ContainmentSingle.set("name", "pOppoT1T2_ContainmentSingle");
        dpOppoT1T2_ContainmentSingle.set("type", defT2);
        dpOppoT1T2_ContainmentSingle.setBoolean("containment", true);
       dpOppoT1T2_ContainmentSingle.setBoolean("nullable", true);

        // create T2 -> T1 prop single -> single
        DataObject dpOppoT2T1_NonContainmentSingle = defT2.createDataObject("property");
        dpOppoT2T1_NonContainmentSingle.set("name", "pOppoT2T1_NonContainmentSingle");
        dpOppoT2T1_NonContainmentSingle.set("type", defT1);
       //dpOppoT2T1_NonContainmentSingle.setBoolean("nullable", true);

        // set as opposites
        dpOppoT1T2_ContainmentSingle.set("opposite", dpOppoT2T1_NonContainmentSingle);
        dpOppoT2T1_NonContainmentSingle.set("opposite", dpOppoT1T2_ContainmentSingle);

        // create T1 -> T2 prop many -> single
        DataObject dpOppoT1T2_ContainmentMany = defT1.createDataObject("property");
        dpOppoT1T2_ContainmentMany.set("name", "pOppoT1T2_ContainmentMany");
        dpOppoT1T2_ContainmentMany.set("type", defT2);
        dpOppoT1T2_ContainmentMany.setBoolean("containment", true);
        dpOppoT1T2_ContainmentMany.setBoolean("many", true);
       dpOppoT1T2_ContainmentMany.setBoolean("nullable", true);

        // create T2 -> T1 prop single -> many
        DataObject dpOppoT2T1_NonContainmentSingle2 = defT2.createDataObject("property");
        dpOppoT2T1_NonContainmentSingle2.set("name", "pOppoT2T1_NonContainmentSingle2");
        dpOppoT2T1_NonContainmentSingle2.set("type", defT1);

        // set as opposites
        dpOppoT1T2_ContainmentMany.set("opposite", dpOppoT2T1_NonContainmentSingle2);
        dpOppoT2T1_NonContainmentSingle2.set("opposite", dpOppoT1T2_ContainmentMany);


        // T1 - T3 props, both ends are non-containment
        // create T1 -> T3 prop single -> single
        DataObject dpOppoT1T3_NonContainmentSingle = defT1.createDataObject("property");
        dpOppoT1T3_NonContainmentSingle.set("name", "pOppoT1T3_NonContainmentSingle");
        dpOppoT1T3_NonContainmentSingle.set("type", defT3);
       dpOppoT1T3_NonContainmentSingle.setBoolean("nullable", true);

        // create T3 -> T1 prop single -> single
        DataObject dpOppoT3T1_NonContainmentSingle = defT3.createDataObject("property");
        dpOppoT3T1_NonContainmentSingle.set("name", "pOppoT3T1_NonContainmentSingle");
        dpOppoT3T1_NonContainmentSingle.set("type", defT1);
       dpOppoT3T1_NonContainmentSingle.setBoolean("nullable", true);

        // set as opposites
        dpOppoT1T3_NonContainmentSingle.set("opposite", dpOppoT3T1_NonContainmentSingle);
        dpOppoT3T1_NonContainmentSingle.set("opposite", dpOppoT1T3_NonContainmentSingle);

        // create T1 -> T3 prop many -> many
        DataObject dpOppoT1T3_NonContainmentMany = defT1.createDataObject("property");
        dpOppoT1T3_NonContainmentMany.set("name", "pOppoT1T3_NonContainmentMany");
        dpOppoT1T3_NonContainmentMany.set("type", defT3);
        dpOppoT1T3_NonContainmentMany.setBoolean("many", true);
        dpOppoT1T3_NonContainmentMany.setBoolean("nullable", true);

        // create T3 -> T1 prop many -> many
        DataObject dpOppoT3T1_NonContainmentMany = defT3.createDataObject("property");
        dpOppoT3T1_NonContainmentMany.set("name", "pOppoT3T1_NonContainmentMany");
        dpOppoT3T1_NonContainmentMany.set("type", defT1);
        dpOppoT3T1_NonContainmentMany.setBoolean("many", true);
       //dpOppoT3T1_NonContainmentMany.setBoolean("nullable", true);

        // set as opposites
        dpOppoT1T3_NonContainmentMany.set("opposite", dpOppoT3T1_NonContainmentMany);
        dpOppoT3T1_NonContainmentMany.set("opposite", dpOppoT1T3_NonContainmentMany);


        typeHelper.define(Arrays.asList(new DataObject[] {defT1, defT2, defT3}));

        // check if define worked fine
        Type t1 = typeHelper.getType(URInProps, "T1");
        Type t2 = typeHelper.getType(URInProps, "T2");
        Type t3 = typeHelper.getType(URInProps, "T3");

        // single
        Property pOppoT1T2_ContainmentSingle = t1.getProperty("pOppoT1T2_ContainmentSingle");
        Property pOppoT2T1_NonContainmentSingle = t2.getProperty("pOppoT2T1_NonContainmentSingle");

        Property pOppoT1T3_NonContainmentSingle = t1.getProperty("pOppoT1T3_NonContainmentSingle");
        Property pOppoT3T1_NonContainmentSingle = t3.getProperty("pOppoT3T1_NonContainmentSingle");

        assert pOppoT1T2_ContainmentSingle    != null;
        assert pOppoT2T1_NonContainmentSingle != null;
        assert pOppoT1T3_NonContainmentSingle != null;
        assert pOppoT3T1_NonContainmentSingle != null;

        assert pOppoT1T2_ContainmentSingle.getOpposite()    == pOppoT2T1_NonContainmentSingle;
        assert pOppoT2T1_NonContainmentSingle.getOpposite() == pOppoT1T2_ContainmentSingle;

        assert pOppoT1T3_NonContainmentSingle.getOpposite() == pOppoT3T1_NonContainmentSingle;
        assert pOppoT3T1_NonContainmentSingle.getOpposite() == pOppoT1T3_NonContainmentSingle;

        // many
        Property pOppoT1T2_ContainmentMany = t1.getProperty("pOppoT1T2_ContainmentMany");
        Property pOppoT2T1_NonContainmentSingle2 = t2.getProperty("pOppoT2T1_NonContainmentSingle2");

        Property pOppoT1T3_NonContainmentMany = t1.getProperty("pOppoT1T3_NonContainmentMany");
        Property pOppoT3T1_NonContainmentMany = t3.getProperty("pOppoT3T1_NonContainmentMany");

        assert pOppoT1T2_ContainmentMany    != null;
        assert pOppoT2T1_NonContainmentSingle2 != null;
        assert pOppoT1T3_NonContainmentMany != null;
        assert pOppoT3T1_NonContainmentMany != null;

        assert pOppoT1T2_ContainmentMany.getOpposite()    == pOppoT2T1_NonContainmentSingle2;
        assert pOppoT2T1_NonContainmentSingle2.getOpposite() == pOppoT1T2_ContainmentMany;

        assert pOppoT1T3_NonContainmentMany.getOpposite() == pOppoT3T1_NonContainmentMany;
        assert pOppoT3T1_NonContainmentMany.getOpposite() == pOppoT1T3_NonContainmentMany;
    }

    /* containment single <-> non-containment single */
    public void testNullablePropsOppoSingleContainment()
    {
        createTypesWithNullableProps();

        Type t1 = typeHelper.getType(URInProps, "T1");
        Type t2 = typeHelper.getType(URInProps, "T2");

        Property pOppoT1T2_ContainmentSingle = t1.getProperty("pOppoT1T2_ContainmentSingle");
        Property pOppoT2T1_NonContainmentSingle = t2.getProperty("pOppoT2T1_NonContainmentSingle");

        // Contaiment Single
        DataObject o1 = dataFactory.create(t1);
        DataObject o2 = o1.createDataObject(pOppoT1T2_ContainmentSingle);

        assertNotNull(o2);

        assertEquals(o2, o1.getDataObject(pOppoT1T2_ContainmentSingle));
        assertEquals(o1, o2.getDataObject(pOppoT2T1_NonContainmentSingle));


        // check nulability
        assertTrue(pOppoT1T2_ContainmentSingle.isNullable());
        assertFalse(pOppoT2T1_NonContainmentSingle.isNullable());

        // Since spec v2.1.1 : SDO-262
        o1.set(pOppoT1T2_ContainmentSingle, null);
        assertFalse(o2.isSet(pOppoT2T1_NonContainmentSingle));

        o1.set(pOppoT1T2_ContainmentSingle, o2);        
        o2.unset(pOppoT2T1_NonContainmentSingle);
        assertFalse(o1.isSet(pOppoT1T2_ContainmentSingle));

        o1.set(pOppoT1T2_ContainmentSingle, o2);
        o1.unset(pOppoT1T2_ContainmentSingle);
        assertFalse(o2.isSet(pOppoT2T1_NonContainmentSingle));
    }

    /* non-containment single <-> non-containment single */
    public void testNullablePropsOppoSingleNonContainment()
    {
        createTypesWithNullableProps();

        Type t1 = typeHelper.getType(URInProps, "T1");
        Type t3 = typeHelper.getType(URInProps, "T3");

        Property pOppoT1T3_NonContainmentSingle = t1.getProperty("pOppoT1T3_NonContainmentSingle");
        Property pOppoT3T1_NonContainmentSingle = t3.getProperty("pOppoT3T1_NonContainmentSingle");

        DataObject o1 = dataFactory.create(t1);
        DataObject o3 = o1.createDataObject(pOppoT1T3_NonContainmentSingle);

        assertNotNull(o3);

        assertEquals(o3, o1.getDataObject(pOppoT1T3_NonContainmentSingle));
        assertEquals(o1, o3.getDataObject(pOppoT3T1_NonContainmentSingle));


        //check nullability
        assertTrue(pOppoT1T3_NonContainmentSingle.isNullable());
        assertTrue(pOppoT3T1_NonContainmentSingle.isNullable());

        // Since spec v2.1.1 : SDO-262
        o1.set(pOppoT1T3_NonContainmentSingle, null);
        assertTrue(o3.get(pOppoT3T1_NonContainmentSingle)==null);

        o1.set(pOppoT1T3_NonContainmentSingle, o3);
        o3.unset(pOppoT3T1_NonContainmentSingle);
        assertFalse(o1.isSet(pOppoT1T3_NonContainmentSingle));

        o1.set(pOppoT1T3_NonContainmentSingle, o3);
        o1.unset(pOppoT1T3_NonContainmentSingle);
        assertFalse(o3.isSet(pOppoT3T1_NonContainmentSingle));
    }

    /* containment many <-> non-containment single */
    public void testNullablePropsOppoManyContainment()
    {
        createTypesWithNullableProps();

        Type t1 = typeHelper.getType(URInProps, "T1");
        Type t2 = typeHelper.getType(URInProps, "T2");

        Property pOppoT1T2_ContainmentMany = t1.getProperty("pOppoT1T2_ContainmentMany");
        Property pOppoT2T1_NonContainmentSingle2 = t2.getProperty("pOppoT2T1_NonContainmentSingle2");

        DataObject o1 = dataFactory.create(t1);
        // contaiment create
        DataObject o2 = o1.createDataObject(pOppoT1T2_ContainmentMany);

        assertNotNull(o2);

        assertEquals(1, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(o2, o1.getList(pOppoT1T2_ContainmentMany).get(0));
        assertEquals(o1, o2.getDataObject(pOppoT2T1_NonContainmentSingle2));

        // unset
        o1.unset(pOppoT1T2_ContainmentMany);
        assertEquals(0, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(false, o2.isSet(pOppoT2T1_NonContainmentSingle2));
        assertNull(o2.getDataObject(pOppoT2T1_NonContainmentSingle2));

        // nullable props
        assertTrue(pOppoT1T2_ContainmentMany.isNullable());
        assertFalse(pOppoT2T1_NonContainmentSingle2.isNullable());


        o1.set(pOppoT1T2_ContainmentMany, o2);
        assertEquals(1, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(o2, o1.getList(pOppoT1T2_ContainmentMany).get(0));
        assertEquals(o1, o2.get(pOppoT2T1_NonContainmentSingle2));
        // set null
        o1.set(pOppoT1T2_ContainmentMany, null);
        assertEquals(1, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(null, o1.getList(pOppoT1T2_ContainmentMany).get(0));
        assertFalse(o2.isSet(pOppoT2T1_NonContainmentSingle2));

        o1.set(pOppoT1T2_ContainmentMany, o2);
        assertEquals(1, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(o2, o1.getList(pOppoT1T2_ContainmentMany).get(0));
        assertEquals(o1, o2.get(pOppoT2T1_NonContainmentSingle2));
        // unset
        o2.unset(pOppoT2T1_NonContainmentSingle2);
        assertEquals(0, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertFalse(o2.isSet(pOppoT2T1_NonContainmentSingle2));

        o1.set(pOppoT1T2_ContainmentMany, o2);
        assertEquals(1, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(o2, o1.getList(pOppoT1T2_ContainmentMany).get(0));
        assertEquals(o1, o2.get(pOppoT2T1_NonContainmentSingle2));
        // list set null
        o1.getList(pOppoT1T2_ContainmentMany).set(0, null);
        assertEquals(false, o2.isSet(pOppoT2T1_NonContainmentSingle2));
        assertEquals(null, o2.getDataObject(pOppoT2T1_NonContainmentSingle2));
        assertEquals(1, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(null, o1.getList(pOppoT1T2_ContainmentMany).get(0));


        // list add
        o1.getList(pOppoT1T2_ContainmentMany).add(o2);
        assertEquals(2, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(o2, o1.getList(pOppoT1T2_ContainmentMany).get(1));
        assertEquals(o1, o2.getDataObject(pOppoT2T1_NonContainmentSingle2));

        // list remove
        o1.getList(pOppoT1T2_ContainmentMany).remove(1);
        assertEquals(1, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(false, o2.isSet(pOppoT2T1_NonContainmentSingle2));
        assertEquals(null, o2.getDataObject(pOppoT2T1_NonContainmentSingle2));

        // list remove null item
        o1.getList(pOppoT1T2_ContainmentMany).remove(0);
        assertEquals(0, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(false, o2.isSet(pOppoT2T1_NonContainmentSingle2));
        assertEquals(null, o2.getDataObject(pOppoT2T1_NonContainmentSingle2));

        // the other set
        o2.set(pOppoT2T1_NonContainmentSingle2, o1);
        assertEquals(1, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(o2, o1.getList(pOppoT1T2_ContainmentMany).get(0));
        assertEquals(o1, o2.getDataObject(pOppoT2T1_NonContainmentSingle2));

        // the other unset
        o2.unset(pOppoT2T1_NonContainmentSingle2);
        assertEquals(0, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(false, o2.isSet(pOppoT2T1_NonContainmentSingle2));
        assertEquals(null, o2.getDataObject(pOppoT2T1_NonContainmentSingle2));


        // sequence add
        Sequence o1seq = o1.getSequence();
        o1seq.add(pOppoT1T2_ContainmentMany, o2);
        assertEquals(1, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(o2, o1.getList(pOppoT1T2_ContainmentMany).get(0));
        assertEquals(o1, o2.getDataObject(pOppoT2T1_NonContainmentSingle2));
        assertEquals(1, o1seq.size());
        assertEquals(pOppoT1T2_ContainmentMany, o1seq.getProperty(0));
        assertEquals(o2, o1seq.getValue(0));

        Sequence o2seq = o2.getSequence();
        assertEquals(1, o2seq.size());
        assertEquals(pOppoT2T1_NonContainmentSingle2, o2seq.getProperty(0));
        assertEquals(o1, o2seq.getValue(0));

        // sequence remove
        o1seq.remove(0);
        assertEquals(0, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(null, o2.getDataObject(pOppoT2T1_NonContainmentSingle2));
        assertEquals(0, o1seq.size());
        assertEquals(0, o2seq.size());


        o1.set(pOppoT1T2_ContainmentMany, o2);
        assertEquals(1, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(o2, o1.getList(pOppoT1T2_ContainmentMany).get(0));
        assertEquals(o1, o2.get(pOppoT2T1_NonContainmentSingle2));
        assertEquals(1, o1seq.size());
        // sequence setValueNull
        o1seq.setValue(0, null);
        assertEquals(1, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(null, o1.getList(pOppoT1T2_ContainmentMany).get(0));
        assertEquals(null, o2.getDataObject(pOppoT2T1_NonContainmentSingle2));
        assertEquals(1, o1seq.size());
        assertEquals(0, o2seq.size());

        o1seq.remove(0);
        assertEquals(0, o1seq.size());


        // the other sequence add
        o2seq.add(pOppoT2T1_NonContainmentSingle2, o1);

        assertEquals(1, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(o2, o1.getList(pOppoT1T2_ContainmentMany).get(0));
        assertEquals(o1, o2.getDataObject(pOppoT2T1_NonContainmentSingle2));
        assertEquals(1, o1seq.size());
        assertEquals(pOppoT1T2_ContainmentMany, o1seq.getProperty(0));
        assertEquals(o2, o1seq.getValue(0));

        assertEquals(1, o2seq.size());
        assertEquals(pOppoT2T1_NonContainmentSingle2, o2seq.getProperty(0));
        assertEquals(o1, o2seq.getValue(0));


        // the other sequence remove
        o2seq.remove(0);
        assertEquals(0, o1.getList(pOppoT1T2_ContainmentMany).size());
        assertEquals(false, o2.isSet(pOppoT2T1_NonContainmentSingle2));
        assertEquals(null, o2.getDataObject(pOppoT2T1_NonContainmentSingle2));
        assertEquals(0, o1seq.size());
        assertEquals(0, o2seq.size());
    }

    /* non-containment many <-> non-containment many */
    public void testNullablePropsOppoManyNonContainment()
    {
        createTypesWithNullableProps();

        Type t1 = typeHelper.getType(URInProps, "T1");
        Type t3 = typeHelper.getType(URInProps, "T3");

        Property pOppoT1T3_NonContainmentMany = t1.getProperty("pOppoT1T3_NonContainmentMany");
        Property pOppoT3T1_NonContainmentMany = t3.getProperty("pOppoT3T1_NonContainmentMany");

        DataObject o1 = dataFactory.create(t1);
        DataObject o3 = dataFactory.create(t3);

        assertNotNull(o3);

        assertEquals(0, o1.getList(pOppoT1T3_NonContainmentMany).size());
        assertEquals(0, o3.getList(pOppoT3T1_NonContainmentMany).size());

        // nullables
        assertTrue(pOppoT1T3_NonContainmentMany.isNullable());
        assertFalse(pOppoT3T1_NonContainmentMany.isNullable());

        // set
        o1.set(pOppoT1T3_NonContainmentMany, o3);
        assertEquals(1, o1.getList(pOppoT1T3_NonContainmentMany).size());
        assertEquals(o3, o1.getList(pOppoT1T3_NonContainmentMany).get(0));
        assertEquals(1, o3.getList(pOppoT3T1_NonContainmentMany).size());
        assertEquals(o1, o3.getList(pOppoT3T1_NonContainmentMany).get(0));

        // list set null
        o1.getList(pOppoT1T3_NonContainmentMany).set(0, null);
        assertEquals(1, o1.getList(pOppoT1T3_NonContainmentMany).size());
        assertEquals(null, o1.getList(pOppoT1T3_NonContainmentMany).get(0));
        assertEquals(0, o3.getList(pOppoT3T1_NonContainmentMany).size());

        // list add
        o1.getList(pOppoT1T3_NonContainmentMany).add(o3);
        assertEquals(2, o1.getList(pOppoT1T3_NonContainmentMany).size());
        assertEquals(o3, o1.getList(pOppoT1T3_NonContainmentMany).get(1));
        assertEquals(1, o3.getList(pOppoT3T1_NonContainmentMany).size());
        assertEquals(o1, o3.getList(pOppoT3T1_NonContainmentMany).get(0));

        // list remove
        o1.getList(pOppoT1T3_NonContainmentMany).remove(1);
        o1.getList(pOppoT1T3_NonContainmentMany).remove(0);
        assertEquals(false, o1.isSet(pOppoT1T3_NonContainmentMany));
        assertEquals(0, o1.getList(pOppoT1T3_NonContainmentMany).size());
        assertEquals(false, o3.isSet(pOppoT3T1_NonContainmentMany));
        assertEquals(0, o3.getList(pOppoT3T1_NonContainmentMany).size());        


        // the other set
        o3.set(pOppoT3T1_NonContainmentMany, o1);
        assertEquals(1, o1.getList(pOppoT1T3_NonContainmentMany).size());
        assertEquals(o3, o1.getList(pOppoT1T3_NonContainmentMany).get(0));
        assertEquals(1, o3.getList(pOppoT3T1_NonContainmentMany).size());
        assertEquals(o1, o3.getList(pOppoT3T1_NonContainmentMany).get(0));

        // the other unset
        o3.unset(pOppoT3T1_NonContainmentMany);
        assertEquals(false, o1.isSet(pOppoT1T3_NonContainmentMany));
        assertEquals(0, o1.getList(pOppoT1T3_NonContainmentMany).size());
        assertEquals(false, o3.isSet(pOppoT3T1_NonContainmentMany));
        assertEquals(0, o3.getList(pOppoT3T1_NonContainmentMany).size());



        // sequence add
        Sequence o1seq = o1.getSequence();
        o1seq.add(pOppoT1T3_NonContainmentMany, o3);
        assertEquals(1, o1.getList(pOppoT1T3_NonContainmentMany).size());
        assertEquals(o3, o1.getList(pOppoT1T3_NonContainmentMany).get(0));
        assertEquals(1, o3.getList(pOppoT3T1_NonContainmentMany).size());
        assertEquals(o1, o3.getList(pOppoT3T1_NonContainmentMany).get(0));
        assertEquals(1, o1seq.size());
        assertEquals(pOppoT1T3_NonContainmentMany, o1seq.getProperty(0));
        assertEquals(o3, o1seq.getValue(0));

        Sequence o3seq = o3.getSequence();
        assertEquals(1, o3seq.size());
        assertEquals(pOppoT3T1_NonContainmentMany, o3seq.getProperty(0));
        assertEquals(o1, o3seq.getValue(0));


        // sequence setValue null
        o1seq.setValue(0, null);
        assertEquals(1, o1.getList(pOppoT1T3_NonContainmentMany).size());
        assertEquals(null, o1.getList(pOppoT1T3_NonContainmentMany).get(0));
        assertEquals(0, o3.getList(pOppoT3T1_NonContainmentMany).size());
        assertEquals(1, o1seq.size());
        assertEquals(null, o1seq.getValue(0));        
        assertEquals(0, o3seq.size());

        assertEquals(true, o1.isSet(pOppoT1T3_NonContainmentMany));
        assertEquals(false, o3.isSet(pOppoT3T1_NonContainmentMany));

        o1.unset(pOppoT1T3_NonContainmentMany);
        assertEquals(0, o1seq.size());
        assertEquals(false, o1.isSet(pOppoT1T3_NonContainmentMany));


        o3seq.add(pOppoT3T1_NonContainmentMany, o1);
        assertEquals(1, o1seq.size());
        assertEquals(o3, o1seq.getValue(0));

        o1seq.setValue(0, null);
        assertEquals(1, o1seq.size());
        assertEquals(null, o1seq.getValue(0));
        assertEquals(0, o3seq.size());

        o1seq.setValue(0, o3);
        assertEquals(1, o1seq.size());
        assertEquals(o3, o1seq.getValue(0));
        assertEquals(1, o3seq.size());
        assertEquals(o1, o3seq.getValue(0));


        // the other sequence setValue
        try
        {
            // must throw IllegalArgumentException
            o3seq.setValue(0, null);
        }
        catch(IllegalArgumentException e)
        {
            assertTrue(true);
        }
        catch (Exception e)
        {
            assertTrue(false);
        }        
    }
}
