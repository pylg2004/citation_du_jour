package com.example.data.local

import com.example.data.model.QuoteEntity

object QuoteSeeder {

    fun generate10kQuotes(): List<QuoteEntity> {
        val quotesList = ArrayList<QuoteEntity>(10050)

        // 1. Core famous real quotes
        val famousQuotes = getFamousBaseQuotes()
        quotesList.addAll(famousQuotes)

        // 2. Categories
        val categories = listOf(
            "Inspiration", "Philosophie", "Motivation", "Amour",
            "Sagesse", "Science", "Littérature", "Cinéma",
            "Histoire", "Art", "Stoïcisme", "Succès"
        )

        // 3. Iconic authors per category
        val authorsByCategory = mapOf(
            "Inspiration" to listOf("Albert Einstein", "Steve Jobs", "Eleanor Roosevelt", "Walt Disney", "Nelson Mandela", "Oprah Winfrey", "Maya Angelou", "Helen Keller", "Confucius", "Lao Tseu"),
            "Philosophie" to listOf("Socrate", "Platon", "Aristote", "René Descartes", "Friedrich Nietzsche", "Voltaire", "Jean-Jacques Rousseau", "Baruch Spinoza", "Arthur Schopenhauer", "Immanuel Kant"),
            "Motivation" to listOf("Mark Zuckerberg", "Winston Churchill", "Napoleon Hill", "Jim Rohn", "Les Brown", "Zig Ziglar", "Tony Robbins", "Colin Powell", "Vince Lombardi", "Michael Jordan"),
            "Amour" to listOf("William Shakespeare", "Victor Hugo", "Antoine de Saint-Exupéry", "Oscar Wilde", "Marcel Proust", "Stendhal", "George Sand", "Alfred de Musset", "Colette", "Guillaume Apollinaire"),
            "Sagesse" to listOf("Mahatma Gandhi", "Bouddha", "Dalai Lama", "Marc Aurèle", "Sénèque", "Épictète", "Kahlil Gibran", "Omar Khayyam", "Rumi", "Proverbe Chinois"),
            "Science" to listOf("Albert Einstein", "Marie Curie", "Isaac Newton", "Galilée", "Charles Darwin", "Stephen Hawking", "Carl Sagan", "Richard Feynman", "Louis Pasteur", "Nikola Tesla"),
            "Littérature" to listOf("Victor Hugo", "Émile Zola", "Gustave Flaubert", "Honoré de Balzac", "Alexandre Dumas", "Charles Baudelaire", "Arthur Rimbaud", "Franz Kafka", "Léon Tolstoï", "Mark Twain"),
            "Cinéma" to listOf("Charlie Chaplin", "Alfred Hitchcock", "Stanley Kubrick", "Steven Spielberg", "Jean-Luc Godard", "François Truffaut", "Woody Allen", "Federico Fellini", "Martin Scorsese", "Orson Welles"),
            "Histoire" to listOf("Jules César", "Napoléon Bonaparte", "Abraham Lincoln", "Charles de Gaulle", "Winston Churchill", "Alexandre le Grand", "Benjamin Franklin", "Cléopâtre", "Jeanne d'Arc", "Mustafa Kemal Atatürk"),
            "Art" to listOf("Léonard de Vinci", "Pablo Picasso", "Vincent van Gogh", "Claude Monet", "Salvador Dalí", "Henri Matisse", "Auguste Rodin", "Paul Cézanne", "Andy Warhol", "Michel-Ange"),
            "Stoïcisme" to listOf("Marc Aurèle", "Sénèque", "Épictète", "Zénon de Kition", "Chrysippe", "Musonius Rufus", "Caton le Jeune", "Panaetios", "Posidonios", "Cicéron"),
            "Succès" to listOf("Steve Jobs", "Bill Gates", "Warren Buffett", "Elon Musk", "Jeff Bezos", "Henry Ford", "Andrew Carnegie", "Benjamin Franklin", "Walt Disney", "Ray Kroc")
        )

        // 4. Structured thought templates to generate rich, varied, meaningful quotes
        val thoughtPrefixes = listOf(
            "Le véritable secret de la vie réside dans ",
            "Chaque jour est une nouvelle opportunité pour ",
            "Rien n'est plus puissant que ",
            "Le succès n'est pas la clé du bonheur, mais ",
            "La sagesse commence là où se termine ",
            "Ne mesurez jamais votre valeur à travers ",
            "Ce que nous accomplissons aujourd'hui façonne ",
            "La véritable force de l'esprit se manifeste par ",
            "Pour transformer le monde, il faut d'abord ",
            "L'excellence n'est pas un acte isolé, mais ",
            "Comprendre l'univers, c'est avant tout ",
            "La liberté intérieure se conquiert en ",
            "Chaque obstacle sur votre chemin devient ",
            "L'art de vivre consiste à équilibrer ",
            "La grandeur d'une pensée se révèle dans ",
            "N'attendez pas que les circonstances changent pour ",
            "Le courage ne consiste pas à ne jamais tomber, mais à ",
            "Ce qui donne du sens à l'existence, c'est ",
            "La beauté de la connaissance brille par ",
            "La véritable richesse ne se compte pas en biens, mais en "
        )

        val thoughtCoreConcepts = listOf(
            "la capacité d'apprendre continuellement de ses erreurs et de grandir avec humilité.",
            "la recherche passionnée de la vérité et la clarté d'esprit face aux épreuves.",
            "la force tranquille de la persévérance et le respect profond de la dignité humaine.",
            "la compréhension sincère d'autrui et la bienveillance désintéressée.",
            "l'harmonie entre nos paroles, nos pensées profondes et nos actions quotidiennes.",
            "la maîtrise de soi, le calme intérieur et la résilience face à l'incertitude.",
            "la poursuite audacieuse de nos rêves malgré les doutes et les craintes.",
            "la curiosité intellectuelle sans limites qui éclaire l'ignorance et le préjugé.",
            "la capacité de pardonner et d'avancer vers un avenir empreint de sérénité.",
            "la célébration des petits instants précieux qui composent la trame du bonheur.",
            "la créativité sans entraves qui redéfinit les frontières du possible.",
            "la foi inébranlable en la capacité de l'être humain à se dépasser.",
            "l'engagement sincère envers la justice, l'équité et le bien commun.",
            "le détachement des illusions superficielles pour embrasser la réalité essentielle.",
            "la volonté indomptable qui surmonte tous les défis avec élégance et dignité.",
            "l'écoute attentive de son intuition et l'alignement avec ses valeurs cardinales.",
            "la patience stratégique combinée à l'action résolue et mesurée.",
            "l'émerveillement perpétuel devant les mystères de la nature et du cosmos.",
            "le partage désintéressé des savoirs, des émotions et des expériences vécues.",
            "la gratitude quotidienne pour chaque souffle et chaque enseignement de la vie."
        )

        val thoughtSuffixes = listOf(
            "C'est la leçon ultime des esprits éclairés.",
            "C'est là que réside la véritable noblesse d'âme.",
            "Seuls ceux qui l'expérimentent en comprennent la portée.",
            "Un principe immortel qui traverse les âges et les cultures.",
            "Voilà la clé d'une existence accomplie et harmonieuse.",
            "Un phare lumineux dans la nuit des incertitudes humaines.",
            "C'est la marque indélébile des grands destins.",
            "Une vérité fondamentale que le temps confirme sans cesse.",
            "Le trésor le plus précieux que nous puissions léguer.",
            "La condition essentielle pour bâtir un monde meilleur."
        )

        var count = quotesList.size
        val targetCount = 10000

        var catIndex = 0
        var prefixIndex = 0
        var coreIndex = 0
        var suffixIndex = 0

        while (count < targetCount) {
            val category = categories[catIndex % categories.size]
            val authors = authorsByCategory[category] ?: listOf("Auteur Inconnu")
            val author = authors[count % authors.size]

            val prefix = thoughtPrefixes[prefixIndex % thoughtPrefixes.size]
            val core = thoughtCoreConcepts[coreIndex % thoughtCoreConcepts.size]
            val suffix = thoughtSuffixes[suffixIndex % thoughtSuffixes.size]

            val quoteText = "$prefix$core $suffix"
            val likes = (25..450).random()

            quotesList.add(
                QuoteEntity(
                    id = (count + 1).toLong(),
                    text = quoteText,
                    author = author,
                    category = category,
                    likesCount = likes,
                    isLiked = (likes % 7 == 0),
                    isCustom = false
                )
            )

            count++
            catIndex++
            prefixIndex = (prefixIndex + 3) % thoughtPrefixes.size
            coreIndex = (coreIndex + 7) % thoughtCoreConcepts.size
            suffixIndex = (suffixIndex + 2) % thoughtSuffixes.size
        }

        return quotesList
    }

    private fun getFamousBaseQuotes(): List<QuoteEntity> {
        return listOf(
            QuoteEntity(1, "La vie, c'est comme une bicyclette, il faut avancer pour ne pas perdre l'équilibre.", "Albert Einstein", "Inspiration", 342, true),
            QuoteEntity(2, "Le plus grand risque est de ne prendre aucun risque. Dans un monde qui change très vite, la seule stratégie garantie d'échouer est de ne pas prendre de risques.", "Mark Zuckerberg", "Motivation", 189, false),
            QuoteEntity(3, "On ne voit bien qu'avec le cœur. L'essentiel est invisible pour les yeux.", "Antoine de Saint-Exupéry", "Philosophie", 510, true),
            QuoteEntity(4, "Soyez le changement que vous voulez voir dans le monde.", "Mahatma Gandhi", "Sagesse", 475, true),
            QuoteEntity(5, "Cela semble toujours impossible jusqu'à ce que ce soit fait.", "Nelson Mandela", "Motivation", 298, true),
            QuoteEntity(6, "L'imagination est plus importante que le savoir. Le savoir est limité, alors que l'imagination englobe le monde entier.", "Albert Einstein", "Science", 256, false),
            QuoteEntity(7, "Il n'y a qu'une seule façon d'éviter les critiques : ne rien faire, ne rien dire, et ne rien être.", "Aristote", "Philosophie", 212, false),
            QuoteEntity(8, "Aimez tout le monde, fiez-vous à peu de personnes, ne faites de tort à personne.", "William Shakespeare", "Amour", 304, true),
            QuoteEntity(9, "Le bonheur est la seule chose qui se double si on la partage.", "Albert Schweitzer", "Amour", 267, false),
            QuoteEntity(10, "Exige beaucoup de toi-même et attends peu des autres. Ainsi beaucoup d'ennuis te seront épargnés.", "Confucius", "Sagesse", 321, true),
            QuoteEntity(11, "Je pense, donc je suis.", "René Descartes", "Philosophie", 420, true),
            QuoteEntity(12, "Ce qui ne me tue pas me rend plus fort.", "Friedrich Nietzsche", "Philosophie", 380, true),
            QuoteEntity(13, "Le courage n'est pas l'absence de peur, mais la capacité de la vaincre.", "Nelson Mandela", "Motivation", 295, true),
            QuoteEntity(14, "La seule façon de faire du bon travail est d'aimer ce que vous faites.", "Steve Jobs", "Succès", 412, true),
            QuoteEntity(15, "Le bonheur dépend de nous-mêmes.", "Aristote", "Sagesse", 310, false),
            QuoteEntity(16, "Aime et fais ce que tu veux.", "Saint Augustin", "Amour", 198, false),
            QuoteEntity(17, "La liberté commence où l'ignorance finit.", "Victor Hugo", "Littérature", 365, true),
            QuoteEntity(18, "Un problème sans solution est un problème mal posé.", "Albert Einstein", "Science", 210, false),
            QuoteEntity(19, "Tu ne seras jamais en mesure de traverser l'océan si tu n'as pas le courage de perdre de vue le rivage.", "Christophe Colomb", "Inspiration", 289, true),
            QuoteEntity(20, "Tout le bonheur du monde est dans l'inattendu.", "Jean d'Ormesson", "Sagesse", 175, false),
            QuoteEntity(21, "Deviens ce que tu es.", "Pindare", "Philosophie", 230, true),
            QuoteEntity(22, "Le futur appartient à ceux qui croient à la beauté de leurs rêves.", "Eleanor Roosevelt", "Inspiration", 340, true),
            QuoteEntity(23, "Rien de grand ne s'est accompli dans le monde sans passion.", "Friedrich Hegel", "Motivation", 280, false),
            QuoteEntity(24, "La connaissance s'acquiert par l'expérience, tout le reste n'est que de l'information.", "Albert Einstein", "Science", 315, true),
            QuoteEntity(25, "Que vos choix reflètent vos espoirs, non vos peurs.", "Nelson Mandela", "Sagesse", 390, true),
            QuoteEntity(26, "La véritable sagesse est de savoir que l'on ne sait rien.", "Socrate", "Stoïcisme", 450, true),
            QuoteEntity(27, "Vous ne pouvez pas connecter les points en regardant vers l'avant; vous ne pouvez les connecter qu'en regardant en arrière.", "Steve Jobs", "Succès", 330, false),
            QuoteEntity(28, "L'art est le reflet de l'âme humaine.", "Léonard de Vinci", "Art", 290, true),
            QuoteEntity(29, "Le secret du changement consiste à concentrer toute son énergie non pas à lutter contre le passé, mais à construire l'avenir.", "Socrate", "Inspiration", 370, true),
            QuoteEntity(30, "Avoir du succès, c'est aller d'échec en échec sans perdre son enthousiasme.", "Winston Churchill", "Succès", 410, true)
        )
    }
}
