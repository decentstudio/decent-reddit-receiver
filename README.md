# decent-reddit-receiver

This is currently a prototype for pulling in data from Reddit. The plan is to make it more general. Currently it pulls in the r/ethtrader daily discussion thread and the initial set of comments for it.

It publishes to two topics:

- `decent.reddit.submission` (The Parent Submission)
- `decent.reddit.comments.chunk` (The chunk of initial comments)

Next steps are:
- Fetch the rest of the comments from the 'more' object.
- Look for the daily thread smarter (using the current date).
- Refactor out boilerplate core.async code.
- Better error handling and logging.
- Plan out the real features and how configurable this should be.
- Add metrics.

## Installation

N/A

## Usage

Development:

- `lein run`

## Options

N/A

## Examples

N/A

### Bugs

N/A

## License

N/A
