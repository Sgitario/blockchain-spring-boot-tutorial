---
layout: post
title: Blockchain using Custom Ethereum Network in Java
date: 2018-11-20
tags: [ Blockchain, Ethereum, Solidity, Java ]
---

We already introduced Blockchain and Ethereum in [an earlier post](https://sgitario.github.io/ethereum-and-solidity-getting-started/). However, there were a couple of points we wanted to explore a bit further:
- How to deploy a custom Ethereum network?
- How to use Java to work with the Ethereum network?

Most of the concepts we saw in [the post](https://sgitario.github.io/ethereum-and-solidity-getting-started/) remain. Therefore, we'll only work with a custom Ethereum network deployed in Docker and revisit the examples and expose them on a REST API in Spring Boot. 

# Deploy a Custom Ethereum Network

In [the previous article](https://sgitario.github.io/ethereum-and-solidity-getting-started/), we worked with [the Rinkeby Ethereum network](https://www.rinkeby.io/#stats). Let's see how to create a local Ethereum network.

We followed [this article](https://piotrminkowski.wordpress.com/2018/06/22/introduction-to-blockchain-with-java-using-ethereum-web3j-and-spring-boot/) where it uses the Go ethereum client [Geth](https://github.com/ethereum/go-ethereum) in order to connect to either the Main or Rinkeby network or to deploy a local network. We'll deploy a local ethereum network using the **--dev** parameter:

```
> docker run -d --name ethereum -p 8545:8545 -p 30303:30303 ethereum/client-go --rpc --rpcaddr "0.0.0.0" --rpcapi="db,eth,net,web3,personal" --rpccorsdomain "*" --dev
```

We can view what the container is doing as:

```
> docker logs -f ethereum
```

Output:

```
INFO [11-20|10:25:02.161] Maximum peer count                       ETH=25 LES=0 total=25
INFO [11-20|10:25:06.497] Using developer account                  address=0xb18b4450B90A21B20ec5eD45df5f1A500E0c476A
INFO [11-20|10:25:06.498] Starting peer-to-peer node               instance=Geth/v1.8.18-unstable-cf3b187b/linux-amd64/go1.11.2
INFO [11-20|10:25:06.498] Writing custom genesis block
INFO [11-20|10:25:06.499] Persisted trie from memory database      nodes=11 size=1.71kB time=91.4µs gcnodes=0 gcsize=0.00B gctime=0s livenodes=1 livesize=0.00B
INFO [11-20|10:25:06.506] Initialised chain configuration          config="{ChainID: 1337 Homestead: 0 DAO: <nil> DAOSupport: false EIP150: 0 EIP155: 0 EIP158: 0 Byzantium: 0 Constantinople: <nil> Engine: clique}"
INFO [11-20|10:25:06.506] Initialising Ethereum protocol           versions="[63 62]" network=1337
INFO [11-20|10:25:06.507] Loaded most recent local header          number=0 hash=214ff5…7bd75b td=1 age=49y7mo5d
INFO [11-20|10:25:06.507] Loaded most recent local full block      number=0 hash=214ff5…7bd75b td=1 age=49y7mo5d
INFO [11-20|10:25:06.507] Loaded most recent local fast block      number=0 hash=214ff5…7bd75b td=1 age=49y7mo5d
INFO [11-20|10:25:06.511] Stored checkpoint snapshot to disk       number=0 hash=214ff5…7bd75b
INFO [11-20|10:25:06.515] started whisper v.6.0
INFO [11-20|10:25:06.516] New local node record                    seq=1 id=5d324a6db168b6c1 ip=127.0.0.1 udp=0 tcp=42017
INFO [11-20|10:25:06.516] Started P2P networking                   self="enode://c1ab7fe7e3fbf1f86184439e2f028b1554faa6a4c7c0bee0ff6f401a8624829709ab5f2e55712c43875d3acf57c98dbb9710b62419d9a20df359ddce29cb0f49@127.0.0.1:42017?discport=0"
INFO [11-20|10:25:06.520] IPC endpoint opened                      url=/tmp/geth.ipc
INFO [11-20|10:25:06.522] HTTP endpoint opened                     url=http://0.0.0.0:8545 cors=* vhosts=localhost
INFO [11-20|10:25:06.522] Transaction pool price threshold updated price=1000000000
INFO [11-20|10:25:06.522] Transaction pool price threshold updated price=1
INFO [11-20|10:25:06.522] Etherbase automatically configured       address=0xb18b4450B90A21B20ec5eD45df5f1A500E0c476A
INFO [11-20|10:25:06.523] Commit new mining work                   number=1 sealhash=d5d34a…a74f6b uncles=0 txs=0 gas=0 fees=0 elapsed=481.5µs
INFO [11-20|10:25:06.523] Sealing paused, waiting for transactions
```

Note we have 11 nodes in our local ethereum network.

## Create Account

Let's create the account we'll use in the examples. First, we need to connect to our container:

```
> docker exec -it ethereum geth attach ipc:/tmp/geth.ipc
```

Now, we can use the Go client to create an account with the mnemonic phrase 'ABC':

```go
> personal.newAccount('ABC')
"0xf4cffcaed2700b9a4e937036a044ea42ecd95e48"
> eth.accounts
["0xb18b4450b90a21b20ec5ed45df5f1a500e0c476a", "0xf4cffcaed2700b9a4e937036a044ea42ecd95e48"]
```

We can find our new account and a pre-existing one with some ether already in.

## Send Ethers to Accounts

Let's send ether from the pre-existing account to our new account:

```go
> eth.sendTransaction({from: eth.accounts[0], to: eth.accounts[1], value: web3.toWei(100000, 'ether')})
"0x65fc53055e970c92d294374c269632937d76d399ed6efafa3206e889bd8f9140"
> eth.getBalance(eth.accounts[1])
1.00000000202221601202e+23
```

We could use "eth.coinbase" in the *from* field if we don't have any account to take the money from. So, we have now all we need in place to continue.

# REST API Application

[In last post](https://sgitario.github.io/ethereum-and-solidity-getting-started/), we developed a React application at frontend using web3j.js framework to connect with [the Metamask](https://metamask.io/) browser plugin and the Rinkby ethereum network. 

Let's build now a REST application using Spring Boot and again the [web3j](https://github.com/web3j/web3j) java framework to connect with our existing Ethereum local network. Also, we'll use Maven to compile the smart contract written in Solidity and generate the sources. Then we'll deploy this contract.

## Maven Dependencies

Let's add [the web3j dependency](https://mvnrepository.com/artifact/org.web3j/core) in our *pom.xml*:

```xml
<dependency>
    <groupId>org.web3j</groupId>
    <artifactId>core</artifactId>
    <version>4.0.1</version>
</dependency>
```

Then, let's configure the web3j plugin to compile and generate the sources from our smart contracts written in Solidity:

```xml
<plugin>
    <groupId>org.web3j</groupId>
    <artifactId>web3j-maven-plugin</artifactId>
    <version>0.3.7</version>
    <configuration>
        <packageName>org.sgitario.lottery.blockchain.model</packageName>
        <nativeJavaType>true</nativeJavaType>
        <outputFormat>java,bin,abi</outputFormat>
        <soliditySourceFiles>
            <directory>src/main/resources/contracts</directory>
            <includes>
                <include>*.sol</include>
            </includes>
        </soliditySourceFiles>
        <outputDirectory>
            <java>src/main/java</java>
            <bin>src/main/resources/bin/generated</bin>
            <abi>src/main/resources/abi/generated</abi>
        </outputDirectory>
    </configuration>
</plugin>
```

The plugin will read the contracts at the path in "src/main/resources/contracts" and generate the sources. In order to run the plugin:

```
> mvn web3j:generate-sources
```

The final contract in Java will be at *org.sgitario.lottery.blockchain.model.Lottery.java*:

```java
package org.sgitario.lottery.blockchain.model;
// ..

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 3.6.0.
 */
public class Lottery extends Contract {
    private static final String BINARY = "6080604052..."; 

    public static final String FUNC_MANAGER = "manager";

    public static final String FUNC_PICKWINNER = "pickWinner";

    public static final String FUNC_GETPLAYERS = "getPlayers";

    public static final String FUNC_ENTER = "enter";

    // ...
}
```

**- Install the Solidity compiler:**

The output after running the web3j plugin is:

```
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building lottery 0.0.1-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO]
[INFO] --- web3j-maven-plugin:0.3.7:generate-sources (default-cli) @ lottery ---
[INFO] Adding to process 'lottery.sol'
[INFO] Solidity Compiler not installed.
[INFO] Solidity Compiler from library is used
```

Note that the plugin will use the in-build 0.4.25 Solidity compiler. What about if we want to use the latest Solidity compiler? That's ok. This plugin will first look for existing installed compilers, so let's install the 0.5.0 Solidity compiler. We're using Homebrew on a MAC:

```
> brew update
> brew upgrade
> brew tap ethereum/ethereum
> brew install solidity
> solc --version
solc, the solidity compiler commandline interface
Version: 0.5.0+commit.1d4f565a.Darwin.appleclang
```

As we can see, the compiler is now at version 0.5.0.

**- Can we generate the sources without the Maven plugin?**

Sure. We can use a Docker image for doing this. Then, we would need to use the web3j tool to generate the Java sources. 

```
> docker run --rm -v src/main/resources:/build ethereum/solc:stable /build/TransactionFee.sol --bin --abi --optimize -o /build
```

The image will compile the contact and will generate the ABI interface in */Lottery.abi* and the bytecode in */Lottery.bin*.

## The Lottery contract

Let's use the same lottery contract as in our previous post. We had to upgrade the Solidity syntax to the latest 5.0 compiler:

```sol
pragma solidity >0.4.99 <0.6.0;

contract Lottery {
    address public manager;
    address payable[] private players;

    constructor() public {
        manager = msg.sender;
    }
    
    function enter() public payable {
        require(msg.value > .01 ether, "Must have at least 0.01 ether");
        
        players.push(msg.sender);
    }
    
    function pickWinner() public restricted {
        uint index = random() % players.length;
        address payable winner = address(players[index]);
        address contest = address(this);
        winner.transfer(contest.balance);
        players = new address payable[](0);
    }
    
    function getPlayers() public restricted view returns (address payable[] memory) {
        return players;
    }
    
    function random() private view returns (uint) {
        return uint(keccak256(abi.encodePacked(block.difficulty, now, players)));
    }
    
    modifier restricted() {
        require(manager == msg.sender, "Only Manager can pick the winner");
        _;
    }
}
```

## Application

First of all, we need to ensure the *Lottery* contract is deployed. Our approach for doing this is to provide the contract address at startup. In case of the contract address is empty, we'll deploy. Otherwise, we won't do anything. See how the *LotteryConfiguration* configuration looks like:

```java
@Configuration
public class LotteryConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(LotteryConfiguration.class);

    @Value("${lottery.contract.owner-address}")
    private String ownerAddress;

    @Value("${web3j.client-address}")
    private String clientAddress;

    @Autowired
    private LotteryContractConfiguration config;

    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(clientAddress, new OkHttpClient.Builder().build()));
    }

    @Bean
    public LotteryService contract(Web3j web3j, @Value("${lottery.contract.address:}") String contractAddress)
            throws Exception {
        if (StringUtils.isEmpty(contractAddress)) {
            Lottery lottery = deployContract(web3j);
            return initLotteryService(lottery.getContractAddress(), web3j);
        }

        return initLotteryService(contractAddress, web3j);
    }

    private LotteryService initLotteryService(String contractAddress, Web3j web3j) {
        return new LotteryService(contractAddress, web3j, config);
    }

    private Lottery deployContract(Web3j web3j) throws Exception {
        LOG.info("About to deploy new contract...");
        Lottery contract = Lottery.deploy(web3j, txManager(web3j), config.gas()).send();
        LOG.info("Deployed new contract with address '{}'", contract.getContractAddress());
        return contract;
    }

    private TransactionManager txManager(Web3j web3j) {
        return new ClientTransactionManager(web3j, ownerAddress);
    }

}
```

Note, the contract address is empty if not set and that we are deploying with the owning account address. See the *application.properties*:

```
web3j.client-address=http://localhost:8545

lottery.contract.owner-address=0xf4cffcaed2700b9a4e937036a044ea42ecd95e48

lottery.contract.gas-price=1
lottery.contract.gas-limit=2000000
lottery.contract.address=0x02a4de6adc30cf1615e2b4889107d9035e23c10a
```

The property *web3j.client-address* is pointing out to our local ethereum network that we deployed in docker. Moreover, we are using a *LotteryProperties* class for all the settings we'll need:

```java
@Configuration
@ConfigurationProperties(prefix = "lottery.contract")
public class LotteryProperties {
    private BigInteger gasPrice;
    private BigInteger gasLimit;

    public StaticGasProvider gas() {
        return new StaticGasProvider(gasPrice, gasLimit);
    }

    // getters and setters
}
```

And this is the *LotteryService* service to work with the *Lottery* contract: 

```java
public class LotteryService {

    private final String contractAddress;
    private final Web3j web3j;
    private final LotteryProperties config;

    public LotteryService(String contractAddress, Web3j web3j, LotteryProperties config) {
        this.contractAddress = contractAddress;
        this.web3j = web3j;
        this.config = config;
    }

    public BigInteger getBalance() throws IOException {
        return web3j.ethGetBalance(contractAddress, DefaultBlockParameterName.LATEST).send().getBalance();
    }

    public void join(String account, BigDecimal ethers) throws Exception {
        Lottery lottery = loadContract(account);
        lottery.enter(Convert.toWei(ethers, Unit.ETHER).toBigInteger()).send();
    }

    @SuppressWarnings("unchecked")
    public List<String> getPlayers(String ownerAddress) throws Exception {
        Lottery lottery = loadContract(ownerAddress);
        return lottery.getPlayers().send();
    }

    public void pickWinner(String ownerAddress) throws Exception {
        Lottery lottery = loadContract(ownerAddress);
        lottery.pickWinner().send();
    }

    private Lottery loadContract(String accountAddress) {
        return Lottery.load(contractAddress, web3j, txManager(accountAddress), config.gas());
    }

    // txManager
}
```

The important notes here is that all the accounts need to load the contract to interact to him. When the account who deployed the contract loads it, then this is the owner of the contract and can do more actions like pick the winner or see the players.

### Controllers

We'll create two controllers: one for restricted actions that only can be accessed by the owner and another for players.

**- OwnerController:**
```java
@RestController
public class OwnerController {

    @Value("${lottery.contract.owner-address}")
    private String ownerAddress;

    @Autowired
    private Web3j web3j;

    @Autowired
    private LotteryService lotteryService;

    @GetMapping("/owner")
    public String getAddress() {
        return ownerAddress;
    }

    @GetMapping("/owner/balance")
    public Balance getBalance() throws IOException {
        EthGetBalance wei = web3j.ethGetBalance(ownerAddress, DefaultBlockParameterName.LATEST).send();

        return new Balance(wei.getBalance());
    }

    @GetMapping("/owner/lottery/players")
    public List<String> getPlayers() throws Exception {
        return lotteryService.getPlayers(ownerAddress);
    }

    @GetMapping("/owner/lottery/pickWinner")
    public void pickWinner() throws Exception {
        lotteryService.pickWinner(ownerAddress);
    }
}
```

**- LotteryController:**

```java
@RestController
public class LotteryController {

    private final LotteryService lotteryService;

    @Autowired
    public LotteryController(LotteryService lotteryService) {
        this.lotteryService = lotteryService;
    }

    @GetMapping("/lottery/balance")
    public Balance getLotteryBalance() throws IOException {
        return new Balance(lotteryService.getBalance());
    }

    @PostMapping("/lottery/join")
    public void joinLottery(@RequestBody Player player) throws Exception {
        lotteryService.join(player);
    }
}
```

# Conclusion

We wanted to learn how to configure web3j from zero. However, there is [a nice Spring Boot starter](https://github.com/web3j/web3j-spring-boot-starter) project that auto configure everything for us (including actuators). One of the more challening pieces was to understand whether we're working in wei or ethers. 1 wei in ethers is almost nothing. So ensure we convert the values into wei's.

See [my Github repository](https://github.com/Sgitario/blockchain-spring-boot-tutorial) for a full example.

