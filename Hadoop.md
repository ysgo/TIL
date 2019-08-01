# [ Hadoop ]

### < 설치 전 준비사항 >

- 가상머신 - 리눅스(centos) 설치
  - 경로는 centos 폴더 안에 가상머신 폴더 새로 생성
  - Single vmdk 선택
  - HDD 40G, Memory 2G, (USB, Sound, Printer) 삭제
  - 언어 - 한국어, 키보드 - 영어(미국) 추가
  - 소프트웨어 선택 - 개발 및 창조를 위한 워크스테이션 선택 - 완료
  - 네트워크 설정 동작시키기 - 호스트이름 master로 변경
  - 파티션 직접 설정 - swap (용량의 10~15%), / (전체 설정)
  - root 비번, 계정 추가할 경우에는 설정추가
  - 설치 후 재부팅
- 설치한 가상리눅스 복제
  - centos 폴더 복사 붙여넣기
  - 폴더명 변경
  - 폴더 안에 확장자 .vmx 파일 텍스트파일로 열기
  - Display 속성 찾아서 속성값을 폴더명과 일치시켜 변경하기
  - vmware player 실행시켜 edit에서 [network] - [advanced] - mac주소 [generate]하여 mac주소 변경
- 복제한 맥 번호
  - m2 = 00:50:56:2C:97:EF
  - m3 = 00:50:56:39:04:5B
  - m4 = 00:50:56:34:82:DB

### < 하둡 완전 분산모드 설치 과정 >

- 각 가상머신별 설정 변경

  ```
  - /etc/sysconfig/network-eno..... 를 vi에디터로 mac주소와 ip주소를 변경
  - hostnamectl set-hostname [변경할 hostname명] 으로 변경(각 가상머신별로 다르게)
  - /etc/hosts 를 vi에디터로 열어 가장 아래에 가상머신별로 ip주소와 hostname을 등록한다(모든 가상머신에
  ```

- SSH Key생성을 통해 다른 시스템과 공유

  ```
  - 비밀번호 없이 로그인이 가능하도록 하는 것으로 다음 명령을 master 시스템에서 수행시켜서 공개키를 만든다.
  (참고 : 시큐어 셸(Secure Shell, SSH)은 네트워크 상의 다른 컴퓨터에 로그인하거나 원격 시스템에서 명령을 실행하고 다른 시스템으로 파일을 복사할 수 있도록 해 주는 응용 프로그램 또는 그 프로토콜을 가리킨다.)
  [hadoop@master ~]$ ssh-keygen -t rsa    
  	Generating public/private rsa key pair.
  	Enter file in which to save the key (/root/.ssh/id_rsa): 그냥엔터
  	Created directory '/root/.ssh'.
  	Enter passphrase (empty for no passphrase) : 그냥엔터
  	Enter same passphrase again: 그냥엔터
  	
  - 공개키를 생성한 다음에는 다음 명령을 수행시켜서 공개키를 나머지 리눅스 시스템과 공유한다.
  	[root@master ~]$ ssh-copy-id  –i  /root/.ssh/id_rsa.pub  root@slave1
  	[root@master ~]$ ssh-copy-id  –i  /root/.ssh/id_rsa.pub  root@slave2
  	[root@master ~]$ ssh-copy-id  –i  /root/.ssh/id_rsa.pub  root@slave3
  	[root@master ~]$ ssh-copy-id  –i  /root/.ssh/id_rsa.pub  root@master
  
  - 각 가상머신으로 접속하여 로그인 암호를 요구할시 설정이 잘못된것
  [root@master ~]$ ssh root@slave1
  Last login: 날짜
  [root@master ~]$ logout
  Connection to slave1 closed
  ---> 이런식으로 등록한 모든 slave, master를 확인한다
  ```

- 홈디렉토리의 .bashrc 파일에 다음 내용을 추가한다.

  ```
  export HADOOP_HOME=/root/hadoop-2.7.7
  export PATH=$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$PATH
  ```

- Hadoop의 설정파일 디렉토리로 옮겨간다

  ```
  cd $HADOOP_HOME/etc/hadoop
  ```

- Hadoop 폴더안에 설정파일에 다음 내용들을 추가한다

  ```
  - hadoop-env.sh 파일 끝에 다음 내용을 추가한다.
  export JAVA_HOME=/usr/local/java
  export HADOOP_HOME=/root/hadoop-2.7.7
  export HADOOP_HEAPSIZE=500
  
  - mapred-env.sh 파일 끝에 다음 내용을 추가한다.
  export JAVA_HOME=/usr/local/java
  export HADOOP_JOB_HISTORYSERVER_HEAPSIZE=500
  export HADOOP_HOME=/root/hadoop-2.7.7
  
  - yarn-env.sh 파일 끝에 다음 내용을 추가한다.
  export JAVA_HOME=/usr/local/java
  export YARN_HEAPSIZE=500
  export HADOOP_HOME=/root/hadoop-2.7.7
  
  (편리한 방식)
  scp mapred-env.sh@slave1:/root/hadoop-2.7.7/etc/hadoop
  현재 mapred-env.sh를 slave1의 폴더경로에 복사해라라는 명령
  ```

- 다음 내용들도 추가

  ```
  - core-site.xml 에 다음 내용을 편집한다.
  <configuration>
     <property>
        <name>fs.defaultFS</name>
        <value>hdfs://master:9000/</value>
     </property>   
  </configuration>
  
  - hdfs-site.xml 에 다음 내용을 편집한다.
  <configuration>
     <property>
        <name>dfs.replication</name>
        <value>3</value>
     </property>
     <property>
        <name>dfs.name.dir</name>
        <value>/root/hadoop-2.7.7/hdfs/name</value>
     </property>
     <property>
        <name>dfs.data.dir</name>
        <value>/root/hadoop-2.7.7/hdfs/data</value>
     </property>
     <property>
        <name>dfs.support.append</name>
        <value>true</value>
     </property>
     <property>
        <name>dfs.namenode.secondary.http-address</name>
        <value>slave1:50090</value>
     </property>
     <property>
        <name>dfs.namenode.secondary.https-address</name>
        <value>slave1:50091</value>
     </property> 
  </configuration>
  
  - mapred-site.xml 에 다음 내용을 편집한다.
  <configuration>
     <property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
     </property>
     <property>
        <name>yarn.resourcemanager.hostname</name>
        <value>master</value>
     </property>
  </configuration>
  
  
  - yarn-site.xml 에 다음 내용을 편집한다. 
  <configuration>
  <!-- Site specific YARN configuration properties -->
     <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
     </property>
  </configuration>
  
  12. slaves 파일을 다음 내용으로 편집한다.
  Localhost를 지우고 slave1, slave2, slave3를 추가한다(반드시 행단위로 작성)
  ```

- 하둡 시스템을 포맷한다(반드시 master에서만 수행해야한다)

  ```
  hdfs namenode -format
  ```

- Hadoop의 HDFS를 기동시킨다

  ```
  start-dfs.sh
  ```

- Hadoop의 HDFS 설치 완료여부를 확인한다

  ```
  jps
  
  - 데몬이 제대로 수행되었는지 확인한다. – jps 사용( JVM Process Status )
      master : NameNode
      slave1, slave2, slave3 : DataNode
      slave1 : SecondaryNameNode
  ```

-  웹 페이지에서 저장된 블록 채크

  ```
  http://master:50070/
  ```

- Hadoop의 HDFS 데몬 종료

  ```
  stop-dfs.sh
  ```

- 하둡 DHFS 영역에 폴더를 구축한다.

  ```
  강사시스템에서 파일 세개를 가져가서 리눅스의 sampledata 디렉토리에 넣고 압축파일은 압축을 푼다.
     ( 압축해제 명령 : bzip2 -kd 2008.csv.bz2 )
  # hdfs dfs -ls /
  # hdfs dfs -mkdir /edudata
  # hdfs dfs -chmod 777 /edudata
  # hafs dfs -put /root/sampledata/파일명   /edudata
  # hdfs dfs -ls /edudata
  ```

- eclipse 설치

  ```
  1.tools폴더에 eclipse 설치
  2.mv eclipse .. (root로 옮기기)
  3.cd eclipse
  4.eclipse 실행(./eclipse)
  ```

- maven 변환

  ```
  1.java project 생성
  2.[configure] - [convert maven project]
  3.pom.xml에 dependency 추가
  <dependency>
  	<groupId>org.apache.hadoop</groupId>
  	<artifactId>hadoop-common</artifactId>
  	<version>2.7.7</version>
  </dependency>
  <dependency>
  	<groupId>org.apache.hadoop</groupId>
  	<artifactId>hadoop-client</artifactId>
  	<version>2.7.7</version>
  </dependency>
  ```

- Java와 Hadoop 연동

  ```
  - hadoop File I/O Dependency
  <dependency>
     <groupId>org.apache.hadoop</groupId>
     <artifactId>hadoop-common</artifactId>
     <version>2.7.7</version>
  </dependency>
  	
  <dependency>
     <groupId>org.apache.hadoop</groupId>
     <artifactId>hadoop-client</artifactId>
     <version>2.7.7</version>
  </dependency>
  <dependency>
     <groupId>org.apache.hadoop</groupId>
     <artifactId>hadoop-mapreduce-client-core</artifactId>
     <version>2.7.7</version>
  </dependency>
  
  <dependency>
     <groupId>org.apache.hadoop</groupId>
     <artifactId>hadoop-annotations</artifactId>
     <version>2.7.7</version>
  </dependency> 
  	
  <dependency>
      <groupId>commons-io</groupId>
      <artifactId>commons-io</artifactId>
      <version>2.6</version>
  </dependency>
   
  <dependency>
     <groupId>org.springframework.data</groupId>
     <artifactId>spring-data-hadoop</artifactId>
     <version>2.5.0.RELEASE</version>
  </dependency>
  	
  		
  <!-- https://mvnrepository.com/artifact/jdk.tools/jdk.tools -->
  <dependency>
  	<groupId>jdk.tools</groupId>
  	<artifactId>jdk.tools</artifactId>
  	<version>1.8</version>
  	<scope>system</scope>
  	<systemPath>C:/Program Files/Java/jdk1.8.0_211/lib/tools.jar</systemPath>
  </dependency>
  ```

- Hadoop HDFS 프로그램 구현

  ```
  // 파일 복사
    Configuration config = new Configuration();
    FileSystem hdfs = FileSystem.get(config);
    Path srcPath = new Path(srcFile);
    Path dstPath = new Path(dstFile);
    hdfs.copyFromLocalFile(srcPath, dstPath);
  
    // 파일 생성
    Configuration config = new Configuration();
    FileSystem hdfs = FileSystem.get(config);
    Path path = new Path(fileName);
    FSDataOutputStream outputStream = hdfs.create(path);
    outputStream.write(buff, 0, buff.length);
  
    // 파일명 변경
    Configuration config = new Configuration();
    FileSystem hdfs = FileSystem.get(config);
    Path fromPath = new Path(fromFileName);
    Path toPath = new Path(toFileName);
    boolean isRenamed = hdfs.rename(fromPath, toPath);
    // 파일 삭제
    Configuration config = new Configuration();
    FileSystem hdfs = FileSystem.get(config);
    Path path = new Path(fileName);
    boolean isDeleted = hdfs.delete(path, false);
  
    // 재귀적으로 파일 삭제
    Configuration config = new Configuration();
    FileSystem hdfs = FileSystem.get(config);
    Path path = new Path(fileName);
    boolean isDeleted = hdfs.delete(path, true);
  
    // 리스트 출력
    Configuration config = new Configuration();
    FileSystem fs = FileSystem.get(URI.create(uri), config);
      
    Path[] paths = new Path[args.length];
       for (int i = 0; i < paths.length; i++) {
          paths[i] = new Path(args[i]);
    }
      
    FileStatus[] status = fs.listStatus(paths);
    Path[] listedPaths = FileUtil.stat2Paths(status);
    for (Path p : listedPaths) {
       System.out.println(p);
    }
  ```

  