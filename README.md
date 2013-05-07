An aspectj library for evaluating the various metrics about hashed collections at runtime, such as:
 * Allocations of zero sized collections
 * Overly sized collections for their contents
 * poorly dispersed items due to poor hashCodes
 * excessive allocations of collections
 
 
 To use apply the aspect to your code with an ant task such as
 
 <target name="aspects">
    <aspectj:iajc outJar="${build.dir}/${your_jar_name}_aj.jar" source="1.6" showWeaveInfo="true">
        <sourceRoots>
            <pathelement location="/home/you/dev/hashshmash/aj"/>
        </sourceRoots>
        <inpath>
             <pathelement location="${build.dir}/${your_jar_name}.jar"/>
        </inpath>
        <classpath>
            <pathelement location="${lib.dir}/aspectjrt.jar"/>
            <path refid="your.classpath"/>
        </classpath>
    </aspectj:iajc>
</target>

Then delete your original jar ${build.dir}/${your_jar_name}.jar and just use ${build.dir}/${your_jar_name}_aj.jar

The aspect will generate a file in ${user.home}/.hashshmash with allocation information in the form

Type \t AllocationTime \t AllocationSite \t CollectionSize \t NumberOfBuckets \t UsedBuckets


 
