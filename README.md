# java-racs

**java-racs** is the java client library for [RACS](https://github.com/racslabs/racs).


## Basic Operations

To open a connection, simply create a new ``Racs`` instance and provide the host and port.

```java
import com.racslabs.Racs;

Racs racs = new Racs("localhost", 6381);
```

### Streaming Audio

The ``pipeline`` function is used to chain together multiple RACS commands and execute them sequentially.
In the below example, a new audio stream is created and opened. Then PCM data is chunked into frames
and streamed to the RACS server.

```java
import com.racslabs.Racs;
import com.racslabs.command.Pipeline;

// Connect to the RACS server
Racs racs = new Racs("localhost", 6381);

// Get pipeline
Pipeline pipeline = racs.pipeline();

// Create a new audio stream using pipeline
pipeline.create("vocals", 44100, 2, 16).execute(); // stream-id, sample-rate, channels, bit-depth

// Reset pipeline
pipeline.reset();

// Prepare array of PCM samples (interleaved L/R, 16- or 24-bit integers)
int[] data = /* your PCM audio data */

// Stream PCM data to the server
racs.stream("vocals")
        .chunkSize(1024 * 32)
        .batchSize(50)
        .compression(true)
        .compressionLevel(8)
        .execute(data);
```
If `chunkSize`, `batchSize`, `compression` and `compressionLevel` are not provided, the default values will be used.

```java
// Stream PCM data to the server
racs.stream("vocals").execute(data);
```

Stream ids stored in RACS can be queried using the ``list`` command. ``list`` takes a glob pattern and returns a `List<String>` object of streams ids matching the pattern.

```java
import com.racslabs.Racs;
import com.racslabs.command.Pipeline;

// Connect to the RACS server
Racs racs = new Racs("localhost", 6381);

// Get pipeline
Pipeline pipeline = racs.pipeline();

// Run list command matching "*" pattern
List<String> result = (List<String>)pipeline.list("*").execute();
```

### Extracting Audio
The below example extracts a 30-second PCM audio segment using the ``range`` command. It then encodes the data to MP3 and writes the resulting bytes to a file.

```java
import com.racslabs.Racs;
import com.racslabs.command.Pipeline;

import java.io.FileOutputStream;
import java.io.IOException;

// Connect to the RACS server
Racs racs = new Racs("localhost", 6381);

// Get pipeline
Pipeline pipeline = racs.pipeline();

// Extract PCM data
// Encode the audio to MP3
byte[] result = (byte[]) pipeline.range("vocals", 0.0, 30.0) // stream-id, start (seconds), duration (seconds)
        .encode("audio/wav")
        .execute();

// Write to a file
try (FileOutputStream fos = new FileOutputStream("vocals.wav")) {
    fos.write(result);
} catch (IOException e) {
    e.printStackTrace();
}
```

### Metadata

Stream metadata can be retrieved using the ``meta`` command. ``meta`` takes the stream id and metadata attribute as parameters.

```java
import com.racslabs.Racs;
import com.racslabs.command.Pipeline;

// Connect to the RACS server
Racs racs = new Racs("localhost", 6381);

// Get pipeline
Pipeline pipeline = racs.pipeline();

// Get sample rate attribute for stream
long result = (long) pipeline.meta("vocals", "sample_rate").execute();
```

The supported metadata attributes are:

| Attribute     | Description                                     |
|---------------|-------------------------------------------------|
| `channels`    | Channel count of the audio stream.              |
| `sample_rate` | Sample rate of the audio stream (Hz).           |
| `bit_depth`   | Bit depth of the audio stream.                  |
| `ref`         | Reference timestamp (milliseconds UTC).         |
| `size`        | Size of the uncompressed audio stream in bytes. |

### Raw Command Execution

To execute raw command strings, use the ``executeCommand`` function.

```java
import com.racslabs.Racs;

Racs racs = new Racs("localhost", 6381);

long result = (long) racs.executeCommand("EVAL '(+ 1 2 3)'");
```

Refer to the documentation in [RACS](https://github.com/racslabs/racs) for the commands.



