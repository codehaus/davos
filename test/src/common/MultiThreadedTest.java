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
package common;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Wing Yew Poon
 */
public class MultiThreadedTest extends BaseTest
{
    public static int QUIET = 0;
    public static int NORMAL = 1;
    public static int VERBOSE = 2;

    private Thread[] threads = null;
    private List<Throwable> results = new ArrayList<Throwable>();
    protected int noiseLevel = NORMAL;

    public MultiThreadedTest(String name)
    {
        super(name);
    }

    protected void dumpResults()
    {
        if (noiseLevel == QUIET)
            return;
        for (Throwable result : results)
        {
            if (noiseLevel == NORMAL)
                System.out.println(result);
            if (noiseLevel == VERBOSE)
                result.printStackTrace(System.out);
        }
    }

    protected void runTestCaseRunnables(TestCaseRunnable[] runnables)
    {
        if ((runnables == null) || (runnables.length == 0))
        {
            throw new IllegalArgumentException("No runnables to run");
        }
        threads = new Thread[runnables.length];
        for (int i = 0; i < threads.length; i++)
        {
            threads[i] = new Thread(runnables[i]);
        }
        for (int i = 0; i < threads.length; i++)
        {
            threads[i].start();
        }
        try
        {
            for (int i = 0; i < threads.length; i++)
            {
                threads[i].join();
            }
        }
        catch(InterruptedException ignore)
        {
            System.out.println("Thread join interrupted");
        }
        threads = null;
        int n = results.size();
        if (n > 0)
        {
            dumpResults();
            fail(n + " thread(s) ended in failure/error");
        }
    }

    protected void handleException(Throwable t)
    {
        synchronized (results)
        {
            results.add(t);
        }
    }

    /*
    public void interruptThreads() 
    {
        if (threads != null)
        {
            for (int i = 0; i < threads.length; i++)
            {
                threads[i].interrupt();
            }
        }
    }
    */

    protected abstract class TestCaseRunnable implements Runnable
    {
        /**
         * Override this to define the test
         */
        public abstract void runTestCase() throws Throwable;

        public void run()
        {
            try
            {
                runTestCase();
            }
            catch (Throwable t)
            {
                handleException(t);
                //interruptThreads();
            }
        }
    }
}
