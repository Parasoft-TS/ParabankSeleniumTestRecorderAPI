# PDA-Selenium-RecorderAPI-Example
This is an example project that demonstrates how to integrate a Selenium project with the Parasoft Recorder REST API.  If you are using another UI testing framework, use this project as a reference for how an integration would be built.

The web application this project is testing is the Parasoft Demo App.
- https://github.com/parasoft/parasoft-demo-app
- https://hub.docker.com/r/parasoft/demo-app

Unmodified Selenium Test:
- See com.parasoft.demo.pda.EndToEndTest.java

Modified Selenium Test (integrated with Parasoft Recorder REST API):
- See com.parasoft.demo.pda.EndToEndTest_WithRecorder.java

Example Java class of how to integrate Parasoft Recorder REST API with any test framework.
- See com.parasoft.recorder.ParasoftRecorder.java

For more information about the Parasoft Recorder:
- See https://docs.parasoft.com/display/SOA20242/Getting+Started+with+SOAtest+Smart+API+Test+Generator
