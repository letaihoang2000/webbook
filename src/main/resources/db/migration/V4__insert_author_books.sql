-- Add image column to authors
ALTER TABLE authors ADD COLUMN image LONGTEXT;

-- Insert authors
INSERT INTO authors (id, name, image, description) VALUES
(
    'f47ac10b-58cc-4372-a567-0e02b2c3d479',
    'Oscar Wilde',
    'https://jvrpympmziwwpedpoagx.supabase.co/storage/v1/object/public/book_content/author/Oscar%20Wilde.jpg',
    'An Irish writer, poet, and prominent aesthete.\n\nOscar Fingal O\'Flahertie Wills Wilde was an Irish playwright, poet, and author of numerous short stories, and one novel.'
),
(
    '6ba7b810-9dad-11d1-80b4-00c04fd430c8',
    'Primo Levi',
    'https://jvrpympmziwwpedpoagx.supabase.co/storage/v1/object/public/book_content/author/Primo%20Levi.jpg',
    'Primo Michele Levi (Turin, 31 July 1919- 11 April 1987) was an Italian Jewish chemist, writer, and Holocaust survivor. He was the author of several books, novels, collections of short stories, essays, and poems.\n\nHe graduated in Chemistry from the University of Turin in 1941 and two years later joined the anti-fascist resistance. He was captured and deported to Auschwitz, where he worked as a slave in an industrial plant. After the camp was liberated by the Red Army in 1945 and after an odyssey through several Eastern European countries, he returned to Turin and published his first account of the extermination camps, *Se questo è un uomo* (*If This Is a Man* / US: *The Reawakening*).'
),
(
    '3d6f8a2e-9b1c-4e5f-a8d7-2c4b6e8f0a1d',
    'L. Frank Baum',
    'https://jvrpympmziwwpedpoagx.supabase.co/storage/v1/object/public/book_content/author/L.%20Frank%20Baum.jpg',
    'Lyman Frank Baum was a US author, poet, playwright, actor, and independent filmmaker best known today as the creator - along with illustrator WW Denslow - of one of the most popular books in US children''s literature, The Wonderful Wizard of Oz.'
),
(
    '4c5d6e7f-8a9b-4c0d-1e2f-3a4b5c6d7e8f',
    'Andrew Robinson',
    'https://jvrpympmziwwpedpoagx.supabase.co/storage/v1/object/public/book_content/author/Andrew%20Robinson.jpg',
    'Andrew Robinson has written more than twenty-five books on an unusual range of subjects: science and the history of science; ancient scripts, writing systems and archaeological decipherment; and Indian history and culture. They include six biographies: of the physicist Albert Einstein (A Hundred Years of Relativity) and the polymath Thomas Young (The Last Man Who Knew Everything); of the decipherers Jean-Francois Champollion (Cracking the Egyptian Code) and Michael Ventris (The Man Who Deciphered Linear B); and of the Indian writer Rabindranath Tagore (The Myriad-Minded Man) and the Indian film director Satyajit Ray (The Inner Eye).\n\nHis most recent books, The Indus: Lost Civilizations, Earth-Shattering Events: Earthquakes, Nations and Civilization, and Einstein on the Run: How Britain Saved the World''s Greatest Scientist, combine his interest in archaeology, history, India and science. He also writes on these subjects for leading magazines and newspapers, such as Nature and The Financial Times.'
);

-- Insert books
INSERT INTO books (id, title, image, description, book_content, published_date, page, price, created_at, author_id, category_id) VALUES
(
    '7f2d9c4e-6a8b-4f1e-9d3c-5a7b9e1f3d5c',
    'Lady Windermere''s Fan: a play about a good woman.',
    'https://jvrpympmziwwpedpoagx.supabase.co/storage/v1/object/public/book_content/book_image/Lady%20Windermere''s%20Fan.jpg',
    'This Victorian comedy of manners sparkles with Wilde''s trademark repartee, epigrams, and witty dialogue. Arch-moralist Lady Windermere, shattered by the suspicion of her husband''s infidelity, contemplates running off with a roue until her rival illustrates the difference between morality and its appearance. A comic masterpiece, studded with humorous quips and clever paradoxes.',
    'https://jvrpympmziwwpedpoagx.supabase.co/storage/v1/object/public/book_content/book_content/Lady%20Windermere''s%20Fan.pdf',
    '1893-01-01',
    132,
    5.99,
    NOW(6),
    'f47ac10b-58cc-4372-a567-0e02b2c3d479',
    7
),
(
    'a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d',
    'The Picture of Dorian Gray',
    'https://jvrpympmziwwpedpoagx.supabase.co/storage/v1/object/public/book_content/book_image/The%20Picture%20of%20dorian%20gray.jpg',
    'The Picture of Dorian Gray is a philosophical novel by Irish writer Oscar Wilde. A shorter novella-length version was published in the July 1890 issue of the American periodical Lippincott''s Monthly Magazine. The novel-length version was published in April 1891.',
    'https://jvrpympmziwwpedpoagx.supabase.co/storage/v1/object/public/book_content/book_content/The%20Picture%20of%20dorian%20gray.pdf',
    '1981-01-01',
    248,
    7.99,
    NOW(6),
    'f47ac10b-58cc-4372-a567-0e02b2c3d479',
    8
),
(
    '5e8f1a2b-3c4d-4e5f-6a7b-8c9d0e1f2a3b',
    'Survival in Auschwitz',
    'https://jvrpympmziwwpedpoagx.supabase.co/storage/v1/object/public/book_content/book_image/Survival%20in%20Auschwitz.jpg',
    'This book describes Primo Levi''s experiences in the concentration camp at Auschwitz during the Second World War. Levi, then a 25-year-old chemist, spent 10 months in Auschwitz before the camp was liberated by the Red Army. Of the 650 Italian Jews in his shipment, Levi was one of only twenty who left the camp alive. The average life expectancy of a new entry was three months. This truly amazing story offers a revealing glimpse into the realities of the Holocaust and its effects on our world. - Back cover.',
    'https://jvrpympmziwwpedpoagx.supabase.co/storage/v1/object/public/book_content/book_content/Survival%20in%20Auschwitz.pdf',
    '1961-01-01',
    160,
    4.85,
    NOW(6),
    '6ba7b810-9dad-11d1-80b4-00c04fd430c8',
    9
),
(
    '9d8c7b6a-5f4e-4d3c-2b1a-0f9e8d7c6b5a',
    'The new Wizard of Oz',
    'https://jvrpympmziwwpedpoagx.supabase.co/storage/v1/object/public/book_content/book_image/The%20new%20Wizard%20of%20Oz.jpg',
    'After a cyclone transports her to the land of Oz, Dorothy must seek out the great Wizard of Oz to return to Kansas.',
    'https://jvrpympmziwwpedpoagx.supabase.co/storage/v1/object/public/book_content/book_content/The%20new%20Wizard%20of%20Oz.pdf',
    '1944-01-01',
    209,
    10.99,
    NOW(6),
    '3d6f8a2e-9b1c-4e5f-a8d7-2c4b6e8f0a1d',
    2
),
(
    '2a3b4c5d-6e7f-4a8b-9c0d-1e2f3a4b5c6d',
    'The sea fairies',
    'https://jvrpympmziwwpedpoagx.supabase.co/storage/v1/object/public/book_content/book_image/The%20sea%20fairies.jpg',
    'This is a tale of life beneath the sea, of mermaids and sea serpents and other strange inhabitants of the ocean depths. A little girl named Trot and Cap''n Bill, an old sailor, are invited by several mermaids to come and visit their under-water home. Baum wrote this story in the hope of interesting his readers in something other than Oz; in the preface he writes: "I hope my readers who have so long followed Dorothy''s adventures in the Land of Oz will be interested in Trot''s equally strange experiences." Of course, he did not succeed in distracting his fans from Oz, yet the book was eagerly read; the result of this attempt was that he was forced to introduce Trot and Cap''n Bill into the later Oz stories.',
    'https://jvrpympmziwwpedpoagx.supabase.co/storage/v1/object/public/book_content/book_content/The%20sea%20fairies.pdf',
    '1911-01-01',
    272,
    6.99,
    NOW(6),
    '3d6f8a2e-9b1c-4e5f-a8d7-2c4b6e8f0a1d',
    2
),
(
    '8f7e6d5c-4b3a-4291-8a0b-9c1d2e3f4a5b',
    'Last Man Who Knew Everything: Thomas Young',
    'https://jvrpympmziwwpedpoagx.supabase.co/storage/v1/object/public/book_content/book_image/THE%20LAST%20MAN%20WHO%20KNEW%20EVERYTHING.jpg',
    'No one has given the polymath Thomas Young (1773–1829) the all-round examination he so richly deserves—until now. Celebrated biographer Andrew Robinson portrays a man who solved mystery after mystery in the face of ridicule and rejection, and never sought fame.',
    'https://jvrpympmziwwpedpoagx.supabase.co/storage/v1/object/public/book_content/book_content/THE%20LAST%20MAN%20WHO%20KNEW%20EVERYTHING.pdf',
    '2023-01-01',
    293,
    20.95,
    NOW(6),
    '4c5d6e7f-8a9b-4c0d-1e2f-3a4b5c6d7e8f',
    4
);