An aspectj library for evaluating the various metrics about hashed collections at runtime, such as:
 * Allocations of zero sized collections
 * Overly sized collections for their contents
 * poorly dispersed items due to poor hashCodes
 * excessive allocations of collections
 
 
 To use, apply the aspect to your code with an ant task such as:
 

    &lt;target name="aspects"&gt;  
        &lt;aspectj:iajc outJar="${build.dir}/${your_jar_name}_aj.jar" source="1.6" showWeaveInfo="true"&gt;  
            &lt;sourceRoots&gt;  
                &lt;pathelement location="/home/you/dev/hashshmash/aj"/&gt;  
            &lt;/sourceRoots&gt;  
            &lt;inpath&gt;  
                &lt;pathelement location="${build.dir}/${your_jar_name}.jar"/&gt;  
            &lt;/inpath&gt;  
            &lt;classpath&gt;  
                &lt;pathelement location="${lib.dir}/aspectjrt.jar"/&gt;  
                &lt;path refid="your.classpath"/&gt;  
            &lt;/classpath&gt;  
        &lt;/aspectj:iajc&gt;  
    &lt;/target&gt;  


Then delete your original jar ${build.dir}/${your_jar_name}.jar and just use ${build.dir}/${your_jar_name}_aj.jar

The aspect will generate a file in ${user.home}/.hashshmash with allocation information in the form

Type \t AllocationTime \t AllocationSite \t CollectionSize \t NumberOfBuckets \t UsedBuckets

You will need aspectjtools.jar to apply the aspect to your code, as well as aspectjrt.jar to run it.


 
