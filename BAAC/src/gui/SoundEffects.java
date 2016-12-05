package gui;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundEffects{
	SoundEffects(){
		
	}
	
	
	public void click(){
		AudioInputStream clickSound = null;
		Clip click = null;
		try {
			clickSound = AudioSystem.getAudioInputStream(getClass().getResource("click.wav"));
		} catch (UnsupportedAudioFileException | IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			click = AudioSystem.getClip(null);
			click.open(clickSound);
			click.start();
		} catch (LineUnavailableException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	public void fanfare(){
		AudioInputStream fanfareSound = null;
		Clip fanfare = null;
		try {
			fanfareSound = AudioSystem.getAudioInputStream(getClass().getResource("fanfare.wav"));
		} catch (UnsupportedAudioFileException | IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			fanfare = AudioSystem.getClip(null);
			fanfare.open(fanfareSound);
			fanfare.start();
		} catch (LineUnavailableException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	public void pop(){
		AudioInputStream popSound = null;
		Clip pop = null;
		try {
			popSound = AudioSystem.getAudioInputStream(getClass().getResource("pop.wav"));
		} catch (UnsupportedAudioFileException | IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			pop = AudioSystem.getClip(null);
			pop.open(popSound);
			pop.start();
		} catch (LineUnavailableException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}