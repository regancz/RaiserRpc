package com.raiser.benchmark.client;

import com.raiser.benchmark.bean.Page;
import com.raiser.benchmark.bean.User;
import com.raiser.benchmark.service.UserService;
import com.raiser.rpc.client.RpcClient;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author: zhengyangxin
 * @date: 9/6/2022 10:47 AM
 */
@BenchmarkMode({Mode.AverageTime})
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 5)
@Threads(4)
@Fork(1)
@State(value = Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ClientBenchmark extends AbstractClient{
    public static final int CONCURRENCY = 32;

    private final RpcClient rpcClient;
    private final UserService userService;

    public ClientBenchmark() {
        rpcClient = new RpcClient("127.0.0.1:2181");
        userService = rpcClient.createService(UserService.class, "1.0");
    }

    @Override
    protected UserService getUserService() {
        return userService;
    }

    @TearDown
    public void close() throws IOException {
        this.rpcClient.stop();
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Override
    public boolean existUser() throws Exception {
        return super.existUser();
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Override
    public boolean createUser() throws Exception {
        return super.createUser();
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Override
    public User getUser() throws Exception {
        return super.getUser();
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Override
    public Page<User> listUser() throws Exception {
        return super.listUser();
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(ClientBenchmark.class.getSimpleName())
                .warmupIterations(3)
                .warmupTime(TimeValue.seconds(10))
                .measurementIterations(3)
                .measurementTime(TimeValue.seconds(10))
                .threads(CONCURRENCY)
                .forks(1)
                .resultFormat(ResultFormatType.CSV)
                .build();

        new Runner(opt).run();
    }
}

