String VOICE = "Bruce"; //Alex

void setup() {
  for (int i=0; i < 3; i++) {
    say("" + i, 1.5f);
  }
}

void sleep(float pSeconds) {
  if (pSeconds>0) {
    try {
      Thread.sleep(int(pSeconds * 1000));
    } 
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}

void draw() {
}

void mousePressed() {
  say("Alex", "#15", 1.0f);
  say("Fred", "Prof. Dr. Honkenstein", 2.5f);
  say("Bruce", "Untitled", 2.0f);
}

void keyPressed() {
  delay("Alex", "#"+(int)(random(1, 100)), random(0, 0.1f));
}

void delay(String pVoice, String pString, float pDelayTime) {
  for (int i=0; i < 3; i++) {
    say(pVoice, pString, pDelayTime);
  }
}

void say(String pString, float pSeconds) {
  say(VOICE, pString);
  sleep(pSeconds);
}

void say(String pVoice, String pString, float pSeconds) {
  say(pVoice, pString);
  sleep(pSeconds);
}


void say(String pVoice, String pString) {
  try {
    Runtime.getRuntime().exec(new String[] {
      "say", pString, "-v", pVoice
    }
    );
  } 
  catch (Exception e) {
    e.printStackTrace();
  }
}

/*
Agnes               en_US    # Isn't it nice to have a computer that will talk to you?
 Albert              en_US    #  I have a frog in my throat. No, I mean a real frog!
 Alex                en_US    # Most people recognize me by my voice.
 Bad News            en_US    # The light you see at the end of the tunnel is the headlamp of a fast approaching train.
 Bahh                en_US    # Do not pull the wool over my eyes.
 Bells               en_US    # Time flies when you are having fun.
 Boing               en_US    # Spring has sprung, fall has fell, winter's here and it's colder than usual.
 Bruce               en_US    # I sure like being inside this fancy computer
 Bubbles             en_US    # Pull the plug! I'm drowning!
 Cellos              en_US    # Doo da doo da dum dee dee doodly doo dum dum dum doo da doo da doo da doo da doo da doo da doo
 Deranged            en_US    # I need to go on a really long vacation.
 Fred                en_US    # I sure like being inside this fancy computer
 Good News           en_US    # Congratulations you just won the sweepstakes and you don't have to pay income tax again.
 Hysterical          en_US    # Please stop tickling me!
 Junior              en_US    # My favorite food is pizza.
 Kathy               en_US    # Isn't it nice to have a computer that will talk to you?
 Pipe Organ          en_US    # We must rejoice in this morbid voice.
 Princess            en_US    # When I grow up I'm going to be a scientist.
 Ralph               en_US    # The sum of the squares of the legs of a right triangle is equal to the square of the hypotenuse.
 Trinoids            en_US    # We cannot communicate with these carbon units.
 Vicki               en_US    # Isn't it nice to have a computer that will talk to you?
 Victoria            en_US    # Isn't it nice to have a computer that will talk to you?
 Whisper             en_US    # Pssssst, hey you, Yeah you, Who do ya think I'm talking to, the mouse?
 Zarvox              en_US    # That looks like a peaceful planet.
 */

/*
NAME
 say - Convert text to audible speech
 
 SYNOPSIS
 say [-v voice] [-r rate] [-o outfile [audio format options] | -n name:port | -a device] [-f file | string ...]
 
 DESCRIPTION
 This tool uses the Speech Synthesis manager to convert input text to
 audible speech and either play it through the sound output device
 chosen in System Preferences or save it to an AIFF file.
 
 OPTIONS
 string
 Specify the text to speak on the command line. This can consist of
 multiple arguments, which are considered to be separated by spaces.
 
 -f file, --input-file=file
 Specify a file to be spoken. If file is - or neither this parameter
 nor a message is specified, read from standard input.
 
 --progress
 Display a progress meter during synthesis.
 
 -v voice, --voice=voice
 Specify the voice to be used. Default is the voice selected in
 System Preferences. To obtain a list of voices installed in the
 system, specify '?' as the voice name.
 
 -r rate, --rate=rate
 Speech rate to be used, in words per minute.
 
 -o out.aiff, --output-file=file
 Specify the path for an audio file to be written. AIFF is the
 default and should be supported for most voices, but some voices
 support many more file formats.
 
 -n name, --network-send=name
 -n name:port, --network-send=name:port
 -n :port, --network-send=:port
 -n :, --network-send=:
 Specify a service name (default "AUNetSend") and/or IP port to be
 used for redirecting the speech output through AUNetSend.
 
 -a ID, --audio-device=ID
 -a name, --audio-device=name
 Specify, by ID or name prefix, an audio device to be used to play
 the audio. To obtain a list of audio output devices, specify '?' as
 the device name.
 
 If the input is a TTY, text is spoken line by line, and the output
 file, if specified, will only contain audio for the last line of the
 input.  Otherwise, text is spoken all at once.
 
 AUDIO FORMATS
 Starting in MacOS X 10.6, file formats other than AIFF may be
 specified, although not all third party synthesizers may initially
 support them. In simple cases, the file format can be inferred from the
 extension, although generally some of the options below are required
 for finer grained control:
 
 --file-format=format
 The format of the file to write (AIFF, caff, m4af, WAVE).
 Generally, it's easier to specify a suitable file extension for the
 output file. To obtain a list of writable file formats, specify '?'
 as the format name.
 
 --data-format=format
 The format of the audio data to be stored. Formats other than
 linear PCM are specified by giving their format identifiers (aac,
 alac). Linear PCM formats are specified as a sequence of:
 
 Endianness (optional)
 One of BE (big endian) or LE (little endian). Default is native
 endianness.
 
 Data type
 One of F (float), I (integer), or, rarely, UI (unsigned
 integer).
 
 Sample size
 One of 8, 16, 24, 32, 64.
 
 Most available file formats only support a subset of these sample
 formats.
 
 To obtain a list of audio data formats for a file format specified
 explicitly or by file name, specify '?' as the format name.
 
 The format identifier optionally can be followed by @samplerate and
 /hexflags for the format.
 
 --channels=channels
 The number of channels. This will generally be of limited use, as
 most speech synthesizers produce mono audio only.
 
 --bit-rate=rate
 The bit rate for formats like AAC. To obtain a list of valid bit
 rates, specify '?' as the rate. In practice, not all of these bit
 rates will be available for a given format.
 
 --quality=quality
 The audio converter quality level between 0 (lowest) and 127
 (highest).
 
 ERRORS
 say returns 0 if the text was spoken successfully, otherwise non-zero.
 Diagnostic messages will be printed to standard error.
 
 EXAMPLES
 say Hello, World
 say -v Alex -o hi -f hello_world.txt
 say -o hi.aac Hello, World
 say -o hi.m4a --data-format=alac Hello, World.
 say -o hi.caf --data-format=LEF32@8000 Hello, World
 
 say -v '?'
 say --file-format=?
 say --file-format=caff --data-format=?
 say -o hi.m4a --bit-rate=?
 
 */
