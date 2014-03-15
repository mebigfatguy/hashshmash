An aspectj library for evaluating the various metrics about hashed collections at runtime, such as:
 * Allocations of zero sized collections
 * Overly sized collections for their contents
 * poorly dispersed items due to poor hashCodes
 * excessive allocations of collections
 
 
 To use, apply the aspect to your code with an ant task such as this:
 

    <target name="aspects" xmlns:artifact="antlib:org.apache.maven.artifact.ant">  
        <aspectj:iajc outJar="${build.dir}/${your_jar_name}_aj.jar" source="1.6" showWeaveInfo="true">  
            <aspectpath>  
                <pathelement location="${lib.dir}/hashshmash-0.1.0.jar"/>  
            </aspectpath>  
            <inpath>  
                <pathelement location="${build.dir}/${your_jar_name}.jar"/>  
            </inpath>  
            <classpath>  
                <pathelement location="${lib.dir}/aspectjrt.jar"/>  
                <path refid="your.classpath"/>  
            </classpath>  
       </aspectj:iajc>  
    </target>
    
    
When compiling the aspectjtools.jar should be in ant/lib

Then delete your original jar ${build.dir}/${your_jar_name}.jar and just use ${build.dir}/${your_jar_name}_aj.jar
You will need to include aspectj.jar and hashshmash.jar in your classpath when running your application.

The aspect will generate a file in ${user.home}/.hashshmash with allocation information in the form

Type \t AllocationTime \t AllocationSite \t CollectionSize \t NumberOfBuckets \t UsedBuckets

You will need aspectjtools.jar to apply the aspect to your code, as well as aspectjrt.jar to run it.


Once you have collected a statistics file by running with aspects, you can generate an html report by running

    java -classpath hashsmhash.jar:aspectjrt.jar:xml-apis.jar:xalan.jar:serializer.jar com.mebigfatguy.hashshmash.Report
    
or if using ant, there is an ant task

    <target name="report" xmlns:report="com.mebigfatguy.hashsmash.report">
        <report:report/>
    </target>
    
    
HashShmash is available on maven.org

    groupId:    com.mebigfatguy.hashshmash
    artifactid: hashshmash
    version:    0.2.0
    
    
    



 
