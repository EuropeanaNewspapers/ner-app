package nl.kb.europeananewspaper.NerAnnotater;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

import edu.stanford.nlp.ie.crf.CRFClassifier;

public class NERClassifiers {

	@SuppressWarnings("rawtypes")
	static Map<Locale, CRFClassifier> classifierMap = new ConcurrentHashMap<Locale, CRFClassifier>();

	static Properties langModels;

	public static void setLanguageModels(Properties langModels) {
		NERClassifiers.langModels = langModels;
	}

	public synchronized static CRFClassifier<?> getCRFClassifierForLanguage(
			Locale lang) {
		if (lang == null) {
			throw new IllegalArgumentException(
					"No language defined for classifier!");
		}

		CRFClassifier<?> classifier = null;

		if (classifierMap.get(lang) == null) {
			// try to load model
			System.out.println("Loading language model for "
					+ lang.getDisplayLanguage());

			// load some defaults for now

			try {
				for (Object langKey : langModels.keySet()) {
					System.out.println(langKey + " -> "
							+ langModels.getProperty(langKey.toString()));
					if (lang.getLanguage().equals(
							new Locale(langKey.toString()).getLanguage())) {
						classifier = CRFClassifier
								.getClassifier(getDefaultInputModelStream(
										langModels.getProperty(langKey
												.toString()), lang));

					}

				}

			} catch (ClassCastException e) {
				throw new IllegalArgumentException(
						"Model does not seem to be the right class ", e);
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException(
						"Class not found while loading model ", e);
			} catch (IOException e) {
				throw new IllegalArgumentException(
						"I/O error while reading class ", e);
			}

			if (classifier == null) {
				throw new IllegalArgumentException(
						"No language model found for language "
								+ lang.getDisplayCountry());
			}
			System.out.println("Done");
			classifierMap.put(lang, classifier);

		}
		return classifierMap.get(lang);

	}

	private static InputStream getDefaultInputModelStream(String modelName,
			Locale lang) {
		InputStream modelStream = null;

		try {
			modelStream = new FileInputStream(modelName);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("Model file not found: "
					+ modelName);
		}

		if (modelName.endsWith(".gz")) {
			try {
				return new GZIPInputStream(modelStream);
			} catch (IOException e) {
				return null;
			}
		} else
			return new BufferedInputStream(modelStream);
	}
}
