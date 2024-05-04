echo "Creating class dir"
mkdir class

echo "Compiling *.java"
cd ./src
javac -cp ../fastutil-7.2.0.jar *.java -d ../class/

echo "Packaging files"
cd ../class/
jar cf TRIEST-FD.jar ./
mv TRIEST-FD.jar ../

echo done.