/*
 * The MIT License
 *
 * Copyright 2017 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.avoid_agent_jna_deadlocks;

import hudson.Extension;
import hudson.FilePath;
import hudson.Functions;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.slaves.ComputerListener;
import hudson.util.jna.GNUCLibrary;
import hudson.util.jna.Kernel32Utils;
import java.io.IOException;
import jenkins.security.MasterToSlaveCallable;

@Extension public class Hack extends ComputerListener {

    @Override public void preOnline(Computer c, Channel channel, FilePath root, TaskListener listener) throws IOException, InterruptedException {
        listener.getLogger().println("Preloading JNA to avoid JENKINS-39179");
        String result;
        try {
            result = channel.call(new PreloadJNA());
        } catch (RuntimeException x) {
            throw new IOException(x);
        }
        listener.getLogger().println("Success! " + result);
    }

    private static class PreloadJNA extends MasterToSlaveCallable<String, RuntimeException> {

        @Override public String call() throws RuntimeException {
            if (Functions.isWindows()) {
                return "System temp dir using kernel32 calls: " + Kernel32Utils.getTempDir();
            } else {
                return "Process ID using glibc call: " + GNUCLibrary.LIBC.getpid();
            }
        }

    }

}
