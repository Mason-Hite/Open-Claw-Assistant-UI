@echo off
echo Cleaning...
rmdir /s /q target 2>nul
mkdir target\classes

echo Compiling...
javac --module-path "C:\Users\caffe\Downloads\openjfx-25.0.2_windows-x64_bin-sdk\javafx-sdk-25.0.2\lib" --add-modules javafx.controls,javafx.fxml -d target\classes ^
src\main\java\OpenClawUI\ui\MainWindow.java ^
src\main\java\OpenClawUI\ui\ChatPanel.java ^
src\main\java\OpenClawUI\ai\OpenClawClient.java ^
src\main\java\OpenClawUI\models\FileNode.java

echo Running...
java --module-path "C:\Users\caffe\Downloads\openjfx-25.0.2_windows-x64_bin-sdk\javafx-sdk-25.0.2\lib" --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics -cp target\classes OpenClawUI.ui.MainWindow
pause
