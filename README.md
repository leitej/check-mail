# check-mail

A simple imap e-mail check.<br/>
It will notify, by icon on system tray, the presence or not of new e-mail.

![systray icon](print.png)

### Description

#### Prerequisites

Java Runtime Environment installed.<br/>
Tested with [OpenJDK](https://openjdk.java.net/).<br/>

#### Install

Download the [release](release.zip) file.
Unzip it.
```
mkdir $HOME/check-mail
unzip release.zip -d $HOME/check-mail/
ls $HOME/check-mail/
```

#### Configuration

Default configurations will be created on first execution. The configurations are located on execution path, so you have to execute always from the same path to catch the same configuration of aplication run.<br/>
<br/>
Run:
```
cd $HOME/check-mail/ && java -jar check-mail.jar
```
Quit the App on systray icon, or killing the process.<br/>
<br/>
Edit file 'meta-inf/leitej.mail.Config.xml', using your mail provider and credentials.
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Object type="xmlom">
	<Config type="leitej.mail.Config" id="1">
		<port type="Integer">993</port>
		<stoppedIcon type="String">./stopped.png</stoppedIcon>
		<noMailIcon type="String">./no_mail.png</noMailIcon>
		<hasMailIcon type="String">./has_mail.png</hasMailIcon>
		<refreshIntervalMS type="Long">120000</refreshIntervalMS>
		<hostname type="String">imap.hostname.com</hostname>
		<username type="String">user@hostname.com</username>
		<password type="String">secret</password>
		<maxConnections type="Integer">4</maxConnections>
	</Config>
</Object>
```

### Start Aplication
<u>It will load the configurations located relative to execution path</u>.
```
cd $HOME/check-mail/ && java -jar check-mail.jar
```

## License

This project is licensed under the GNU General Public License v3.0 License - see the [LICENSE](LICENSE) file for details
