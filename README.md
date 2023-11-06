reload-quartz
==============


[![java/maven build](https://github.com/silviuilie/reload-quartz/actions/workflows/maven.yml/badge.svg)](https://github.com/silviuilie/reload-quartz/actions/workflows/maven.yml)
![Code Coverage](https://img.shields.io/badge/Code%20Coverage-50.00-critical?style=flat)
[![Dependency Status](https://www.versioneye.com/user/projects/54436bbf53acfaccc8000025/badge.svg?style=flat)](https://www.versioneye.com/user/projects/54436bbf53acfaccc8000025)
 
[quartz-scheduler](http://quartz-scheduler.org/) utility 

why  
=

if you need to change quartz (>=2.2.1) scheduled job/trigger states (or CronTrigger cron expressions) at runtime.


how to use
=

1. as a Servlet
---

in the deployment descriptor of your application the following context parameters must be used to configure the servlet :

- quartz-authorization-class class name of the `QuartzUtilityAuthorization` implementation. **optional parameter**, if not defined `eu.pm.tools.quartz.DefaultQuartzUtilityAuthorization` will be used.
- quartz-jsp-location, location of the JSP files. **optional parameter** , if not defined `/WEB-INF/jsp` will be used.

(?)
probably the Servlet, rather than Controller configuration should be used since it is easier to configure.

the utility is available by default at `<context>/quartz.quartz`. see `src/main/resources/META-INF/web-fragment.xml` for details.

2. as a spring Controller
---
  create a bean for QuartzApplicationContext to describe the *host* application :

* authorization to be performed on the actions.

add a `QuartzApplicationContext` spring bean in your spring context

     @Bean
     public QuartzApplicationContext quartzjApplicationContext() {
         return new QuartzApplicationContext(QuartzApplicationContext.DEFAULT_AUTHORIZATION);
     }

The default authorization ( `QuartzApplicationContext.DEFAULT_AUTHORIZATION` ) allows access to quartz
utilities to *all requests*. To restrict access, create your own authorization by implementing `QuartzUtilityAuthorization` :


    @Bean
    public CustomAuthorization implements QuartzUtilityAuthorization {
        @Override
        public boolean authorize(HttpSession session) {
            // your auth. code ..
        }
    }

and initialize `QuartzApplicationContext` using it.




#### configure spring component scanner to include `eu.pm.tools.quartz` package


    @ComponentScan(
            basePackages = {
                    // your packages
                    "your.packages",
                    "eu.pm.tools.quartz"
            }
    )

(?) This is needed to enable @Controller class that manages the utility requests.




#### web.xml configuration

Add the the *.quartz url extension to your DispatcherServlet :

    <servlet-mapping>
        <servlet-name>your-spring-servlet-dispatcher-name</servlet-name>
        <url-pattern>*.quartz</url-pattern>
    </servlet-mapping>

(?) This is needed to enable your DispatcherServlet to serve the utility requests :



- `quartz.quartz` is the "home" of the utility, loads the UI,
- see QuartzUtility.QUARTZ_UTILITY_* constants for all requests



#### JSP configuration

If the host application JSPs are not located in the (more or less) standard location `/WEB-INF/jsp/`, add a new
view resolver in your spring configuration with the `/WEB-INF/jsp/`prefix.
Otherwise, no other configuration is required.

