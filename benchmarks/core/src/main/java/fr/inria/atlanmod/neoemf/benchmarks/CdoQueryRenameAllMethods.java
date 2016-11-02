/*
 * Copyright (c) 2013-2016 Atlanmod INRIA LINA Mines Nantes.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlanmod INRIA LINA Mines Nantes - initial API and implementation
 */

package fr.inria.atlanmod.neoemf.benchmarks;

import fr.inria.atlanmod.neoemf.benchmarks.cdo.EmbeddedCDOServer;
import fr.inria.atlanmod.neoemf.benchmarks.queries.JavaQueries;
import fr.inria.atlanmod.neoemf.benchmarks.util.MessageUtil;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.emf.cdo.net4j.CDONet4jSession;
import org.eclipse.emf.cdo.session.CDOSession;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.ecore.resource.Resource;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CdoQueryRenameAllMethods {

    private static final Logger LOG = Logger.getLogger(CdoQueryRenameAllMethods.class.getName());

    private static final String IN = "input";

    private static final String REPO_NAME = "reponame";

    private static final String EPACKAGE_CLASS = "epackage_class";

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption(Option.builder(IN)
                .argName("INPUT")
                .desc("Input CDO resource directory")
                .numberOfArgs(1)
                .required()
                .build());

        options.addOption(Option.builder(EPACKAGE_CLASS)
                .argName("CLASS")
                .desc("FQN of EPackage implementation class")
                .numberOfArgs(1)
                .required()
                .build());

        options.addOption(Option.builder(REPO_NAME)
                .argName("REPO_NAME")
                .desc("CDO Repository name")
                .numberOfArgs(1)
                .required()
                .build());

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine commandLine = parser.parse(options, args);

            String repositoryDir = commandLine.getOptionValue(IN);
            String repositoryName = commandLine.getOptionValue(REPO_NAME);

            Class<?> inClazz = CdoQueryRenameAllMethods.class.getClassLoader().loadClass(commandLine.getOptionValue(EPACKAGE_CLASS));
            inClazz.getMethod("init").invoke(null);

            EmbeddedCDOServer server = new EmbeddedCDOServer(repositoryDir, repositoryName);
            try {
                server.run();
                CDOSession session = server.openSession();
                ((CDONet4jSession) session).options().setCommitTimeout(50 * 1000);
                CDOTransaction transaction = session.openTransaction();
                Resource resource = transaction.getRootResource();

                String name = UUID.randomUUID().toString();
                {
                    LOG.log(Level.INFO, "Start query");
                    long begin = System.currentTimeMillis();
                    JavaQueries.renameAllMethods(resource, name);
                    long end = System.currentTimeMillis();
                    transaction.commit();
                    LOG.log(Level.INFO, "End query");
                    LOG.log(Level.INFO, MessageFormat.format("Time spent: {0}", MessageUtil.formatMillis(end - begin)));
                }

//				{
//					transaction.close();
//					session.close();
//					
//					session = server.openSession();
//					transaction = session.openTransaction();
//					resource = transaction.getRootResource();
//					
//					EList<? extends EObject> methodList = JavaQueries.getAllInstances(resource, JavaPackage.eINSTANCE.getMethodDeclaration());
//					int i = 0;
//					for (EObject eObject: methodList) {
//						MethodDeclaration method = (MethodDeclaration) eObject;
//						if (name.equals(method.getName())) {
//							i++;
//						}
//					}
//					LOG.log(Level.INFO, MessageFormat.format("Renamed {0} methods", i));
//				}

                transaction.close();
                session.close();
            }
            finally {
                server.stop();
            }
        }
        catch (ParseException e) {
            MessageUtil.showError(e.toString());
            MessageUtil.showError("Current arguments: " + Arrays.toString(args));
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar <this-file.jar>", options, true);
        }
        catch (Throwable e) {
            MessageUtil.showError(e.toString());
        }
    }
}
