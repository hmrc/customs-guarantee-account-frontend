
# customs-guarantee-account-frontend

A frontend component for the CDS Financials project which aims to allow the user to download and view guarantee account details

## Running the application locally

The application should be run as part of the CUSTOMS_FINANCIALS_ALL profile due to it being an integral part of service.

Once these services are running, you should be able to do `sbt "run 9395"` to start in `DEV` mode or
`sbt "start -Dhttp.port=9395"` to run in `PROD` mode.

## Running tests

There is just one test source tree in the `test` folder. Use `sbt test` to run them.

To get a unit test coverage report, you can run `sbt clean coverage test coverageReport`,
then open the resulting coverage report `target/scala-2.12/scoverage-report/index.html` in a web browser.

Test coverage threshold is set at 80% - so if you commit any significant amount of implementation code without writing tests, you can expect the build to fail.

## All tests and checks

This is an sbt command alias specific to this project. It will run a scala style check, run unit tests, run integration tests and produce a coverage report.

> `sbt runAllChecks`
