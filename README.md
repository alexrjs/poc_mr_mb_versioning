# poc_mr_mb_versioning
P.o.C. for auto versioning for a Multi-Repo / Multi-Branch Jenkinsfile Job structure

## How to use
Steps:  
1. Clone this template repo
2. For each sub folder create it's own github/git repo.
3. Put the files in each sub folder into the assosiated repo. TODO: Do not forget to visit each file and do adjustments of pathes, names, aso.
4. Push it as master. 
5. Then for each repo create a development branch and push it.  
   --> at this point you should have 4 repos with two branches (master, development each)
6. Go to your jenkins of your choice.  
   --> I used the stock jenkins image from Cloudbee, with a persistent home folder and no extra plug-ins, just the default set which you can install during setup
7. Open the Jenkins Skript Console and enter the following script:  
~~~~
import jenkins.model.Jenkins
import jenkins.plugins.git.*
import hudson.triggers.*
import com.cloudbees.hudson.plugins.folder.Folder
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject
import jenkins.branch.BranchProperty
import jenkins.branch.BranchSource
import jenkins.branch.DefaultBranchPropertyStrategy
import org.jenkinsci.plugins.workflow.libs.*

def parent = Jenkins.instance
println "parent: $parent"

// create folder
Folder folder = parent.getItemByFullName('poc_versions')
if (folder == null) {
  folder = parent.createProject(Folder.class, "poc_versions")
  folder.displayName = "P.O.C Versions Jobs"
  folder.getProperties().add(
    new FolderLibraries(
      Collections.singletonList(
        new LibraryConfiguration(
          "versions", 
          new SCMSourceRetriever(
            new GitSCMSource(
              null, 
              "ssh://git@servix/extra/git/poc_main.git",  // TODO: adjust to your main repo here
              "local_git", // TODO: Do not forget to adjust the credentials here
              "*", 
              "", 
              true)
          )
        )
      )
    )
  )
}
println "folder: $folder"

def jobnames = ['poc_comp_1', 'poc_comp_2', 'poc_comp_3', 'poc_main'] // TODO: adjust to your job naming of the repositories here
def branches = ['master', 'development']
jobnames.each { jobname ->
  println "jobname: $jobname"
  WorkflowMultiBranchProject mbp = Jenkins.instance.getItemByFullName("poc_versions/p_${jobname}_mb") // TODO: adjust to your job naming convention here
  if (mbp != null) {
    mbp.delete()
  } 
  
  def repo = "ssh://git@servix/extra/git/${jobname}.git" // TODO: adjust to your repo here
  println "repo: $repo"

  mbp = folder.createProject(WorkflowMultiBranchProject.class, "p_${jobname}_mb") // TODO: adjust to your job naming convention here
  mbp.displayName = "Git: $jobname [build/test *]"
  mbp.getSourcesList().add(new BranchSource(new GitSCMSource(null, "${repo}", "local_git", "*", "", false), new DefaultBranchPropertyStrategy(new BranchProperty[0]))); // TODO: Do not forget to adjust the credentials here
  mbp.scheduleBuild2(0).getFuture().get()
  println "mbp: $mbp"
  
  // TODO: Do not forget to adjust the user and email
  ParametersDefinitionProperty pdp = new ParametersDefinitionProperty(
    new ChoiceParameterDefinition("BRANCH_USER", "<name>", "Branch User"),
    new ChoiceParameterDefinition("BRANCH_EMAIL", "<email>", "Branch Email"),
    new ChoiceParameterDefinition("BRANCH_REPO", "${repo}", "Branch Repo")
  )
  println "params: $pdp"

  // loop through the branches
  branches.each { branch ->
    job = mbp.getItem(branch)
    if (job != null) {
      println "job $branch: $job"
      job.displayName = "Branch: $branch"
      job.addProperty(pdp)
    }
  }
}

// reload jobs
parent.reload()

~~~~
8. Control the generated jobs
   --> They should have run each once
   --> Sometimes jenkins is so fast, that a job fails, just re-build. Usually the second time it is ok. (First time the parameters where not set, yet)
9. Commit something into the development branches, and watch the magic happening
10. At last merge the development to master (and do not forget to merge back to development)

## A tip on the side
If you have access to your server side git folders. You can trigger jenkins jobs after a push, with this little script (post-update) in the hooks folder:
~~~~
#!/bin/sh
#
# An example hook script to prepare a packed repository for use over
# dumb transports.
#
# To enable this hook, rename this file to "post-update".

#exec git update-server-info
## Adjust the two following lines for your needs 
repo=$(echo $PWD | cut -d / -f 4)
curl -s http://<server>:<port>/git/notifyCommit?url=<repo_url_base>/$repo
~~~~

## License
See extra LICENSE file
Would be nice if you drop me a line, if you find this usefull

   
  
  
Enjoy
