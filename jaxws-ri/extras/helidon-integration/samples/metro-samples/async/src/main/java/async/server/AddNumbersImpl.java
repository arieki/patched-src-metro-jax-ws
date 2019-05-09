/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package async.server;

import javax.jws.WebService;

@WebService(serviceName = "AddNumbersService", targetNamespace = "http://duke.example.org")
public class AddNumbersImpl {

    public int addNumbers(int number1, int number2) {
        System.out.println("Received Request!");
        System.out.println("Sleeping for 5 seconds");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return number1 + number2;
    }
}