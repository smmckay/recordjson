package us.abbies.b.recordjson.tokens;

import com.fasterxml.jackson.core.JsonFactory;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.io.StringReader;

public class TokenizerBenchmark {
    @State(Scope.Benchmark)
    public static class BenchmarkState {
        @Param({"12"})
        int jsonSeed;
        String json;
        JsonFactory jf;

        @Setup
        public void generateJson() {
            json = new JsonGenerator(jsonSeed).generate();
            jf = JsonFactory.builder().build();
            System.out.format("Generated %d characters of JSON\n", json.length());
        }
    }

    @Benchmark
    public void tokenizeRecordjson(BenchmarkState state) {
        var t = new Tokenizer(new StringReader(state.json));
        while (t.hasNext()) {
            t.next();
        }
    }

    @Benchmark
    public void tokenizeJackson(BenchmarkState state) throws IOException {
        var p = state.jf.createParser(new StringReader(state.json));
        while (p.nextToken() != null) {}
    }
}
