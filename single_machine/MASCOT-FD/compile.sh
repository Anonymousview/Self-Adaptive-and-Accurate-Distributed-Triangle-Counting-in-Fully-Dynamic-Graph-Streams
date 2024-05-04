echo "Creating class dir"
mkdir class

echo "Compiling *.java"
cd ./src
javac -cp ../fastutil-7.2.0.jar *.java -d ../class/

echo "Packaging files"
cd ../class/
jar cf MASCOT-FD.jar ./
mv MASCOT-FD.jar ../

echo done.