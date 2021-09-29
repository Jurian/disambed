# Creating knowledge graph embeddings


## Compiling into runnable jar
Create a jar with:
> mvn clean package assembly:single

The program needs a configuration file to run. Examples can be found in the /config directory

## Then run with:
> java -jar target/graph-embeddings.jar <additional jvm args> -c <config file>
For example:
> java -jar target/graph-embeddings.jar -Xmx30g -c config/saa.yml

When the process is finished, the generated embedding will be in the /out directory
