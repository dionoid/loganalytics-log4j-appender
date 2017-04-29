# loganalytics-log4j-appender
Log4j 1.2 appender for pushing log events to Azure Log Analytics. It uses the HTTP Data Collector API.

Example configuration in log4j.xml:

```xml
...
<appender name="loganalytics" class="com.dionoid.log4j.LogAnalyticsAppender">
    <param name="WorkspaceId" value="[Your WorkspaceId here!]" />
    <param name="SharedKey" value="[Your Primary or Secondary Key here!]" />
    <param name="LogType" value="Test" />
    <!-- note: the log Type will get a '_CL' suffix in Log Analytics to distinguish it as a Custom Log -->
</appender>
...
```

Your **WorkspaceId** and **SharedKey** are part of your Azure Log Analytics subscription.

The **LogType** is the name that identifies your 'Type' in Azure Log Analytics, mostly used to identify your application or source.
*Note that the log Type will get a '_CL' suffix in Log Analytics to distinguish it as a Custom Log.*

For best performance, wrap the LogAnalyticsAppender inside an AsyncAppender, so logging won't hold up execution of your code:

```xml
...
<appender name="async" class="org.apache.log4j.AsyncAppender">
    <appender-ref ref="loganalytics"/>
</appender>
...
...
<root>
    <level value="INFO" />
    <appender-ref ref="async"/>
</root>
...
```
